package com.liboshuai.slr.module.connector.service.kafkaEvent.impl;

import cn.hutool.core.collection.CollUtil;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.object.reflect.ReflectUtils;
import com.liboshuai.slr.framework.common.util.object.reflect.SFunction;
import com.liboshuai.slr.module.admin.api.riskRule.RuleTargetApi;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleTargetDTO;
import com.liboshuai.slr.module.connector.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventErrorRespVO;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventReqVO;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaInfoRespVO;
import com.liboshuai.slr.module.connector.convert.KafkaEventConvert;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.KafkaEventErrorDO;
import com.liboshuai.slr.module.connector.dal.kafka.provider.KafkaEventProvider;
import com.liboshuai.slr.module.connector.dal.mongo.KafkaEventErrorRepository;
import com.liboshuai.slr.module.connector.enums.KafkaEventErrorLevelEnum;
import com.liboshuai.slr.module.connector.service.kafkaEvent.KafkaEventService;
import com.liboshuai.slr.module.connector.service.kafkaEvent.strategy.KafkaEventStrategy;
import com.liboshuai.slr.module.connector.service.kafkaEvent.strategy.KafkaEventStrategyHolder;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KafkaEventServiceImpl implements KafkaEventService {

    @Resource
    private KafkaEventConvert kafkaEventConvert;

    @Resource
    private KafkaEventProvider kafkaEventProvider;

    @Resource
    private KafkaEventStrategyHolder kafkaEventStrategyHolder;

    @Resource
    private RuleTargetApi ruleTargetApi;

    @Resource
    private KafkaEventErrorRepository kafkaEventErrorRepository;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * 业务平台上送事件数据到 kafka
     */
    @Override
    public void push(KafkaEventGroupReqVO kafkaEventGroupReqVO) {
        log.info("业务平台上送事件数据: {}", kafkaEventGroupReqVO);
        String channel = kafkaEventGroupReqVO.getChannel(); // 渠道
        List<KafkaEventReqVO> kafkaEventReqVOList = kafkaEventGroupReqVO.getKafkaEventReqVOList(); // 上送事件详情集合
        // 若存在非法数据，直接中止推送操作
        validateAndAbortPush(kafkaEventGroupReqVO);
        // 只过滤掉非法数据继续推荐，并给出非法数据错误原因
        List<KafkaEventErrorRespVO> kafkaEventErrorRespVOList = validateAndFilterInvalidData(kafkaEventReqVOList);
        // 过滤没有上线的目标、事件、事件属性 数据
        validateRuleTargetEvent(kafkaEventGroupReqVO);
        // req转dto
        List<KafkaEventDTO> kafkaEventDTOList = kafkaEventReqVOList.stream()
                .map(kafkaEventReqVO -> kafkaEventConvert.convertReq2Dto(kafkaEventReqVO)) // 转换为DTO
                .map(kafkaEventDTO -> kafkaEventDTO.setChannel(channel)) // 设置渠道
                .collect(Collectors.toList());
        // 各渠道特别的数据处理逻辑
        KafkaEventStrategy kafkaEventStrategy = kafkaEventStrategyHolder.getByChannel(channel);
        kafkaEventStrategy.processAfter(kafkaEventDTOList);
        // 异步推送数据到kafka
        kafkaEventProvider.batchSend(kafkaEventDTOList);
        // 存在非法数据错误原因，则抛出异常
        if (!CollectionUtils.isEmpty(kafkaEventErrorRespVOList)) {
            // 构建错误数据对象，并存入 mongodb
            KafkaEventErrorDO kafkaEventErrorDO = KafkaEventErrorDO.builder()
                    .channel(channel)
                    .level(KafkaEventErrorLevelEnum.MAJOR.getCode())
                    .cause(JsonUtils.toJsonString(kafkaEventErrorRespVOList))
                    .data(JsonUtils.toJsonString(kafkaEventGroupReqVO))
                    .time(LocalDateTime.now())
                    .build();
            kafkaEventErrorRepository.insert(kafkaEventErrorDO);
            throw ServiceExceptionUtil.exception(kafkaEventErrorRespVOList, ErrorCodeConstants.UPLOAD_EVENT_MINOR_ERROR);
        }
    }

    private void validateRuleTargetEvent(KafkaEventGroupReqVO kafkaEventGroupReqVO) {
        String channel = kafkaEventGroupReqVO.getChannel();
        List<RuleTargetDTO> ruleTargetDTOList = ruleTargetApi.getCacheDetailList();
        if (CollectionUtils.isEmpty(ruleTargetDTOList)) {
            // 构建错误数据对象，并存入 mongodb
            String message = "因上线的规则目标事件配置项条数为0，故不接受业务平台任何上送数据";
            KafkaEventErrorDO kafkaEventErrorDO = KafkaEventErrorDO.builder()
                    .channel(channel)
                    .level(KafkaEventErrorLevelEnum.MAJOR.getCode())
                    .cause(message)
                    .data(JsonUtils.toJsonString(kafkaEventGroupReqVO))
                    .time(LocalDateTime.now())
                    .build();
            kafkaEventErrorRepository.insert(kafkaEventErrorDO);
            throw ServiceExceptionUtil.exception(message, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
        }
        ruleTargetDTOList = ruleTargetDTOList.stream()
                .filter(ruleTargetDTO -> Objects.equals(channel, ruleTargetDTO.getChannel()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleTargetDTOList)) {
            // 构建错误数据对象，并存入 mongodb
            String message = String.format("因 [%s] 渠道上线的规则目标事件配置项条数为0，故不接受业务平台任何上送数据", channel);
            KafkaEventErrorDO kafkaEventErrorDO = KafkaEventErrorDO.builder()
                    .channel(channel)
                    .level(KafkaEventErrorLevelEnum.MAJOR.getCode())
                    .cause(message)
                    .data(JsonUtils.toJsonString(kafkaEventGroupReqVO))
                    .time(LocalDateTime.now())
                    .build();
            kafkaEventErrorRepository.insert(kafkaEventErrorDO);
            throw ServiceExceptionUtil.exception(message, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
        }
        for (RuleTargetDTO ruleTargetDTO : ruleTargetDTOList) {
            String ruleTargetField = ruleTargetDTO.getTargetField();
            List<KafkaEventReqVO> kafkaEventReqVOList = kafkaEventGroupReqVO.getKafkaEventReqVOList();
            for (KafkaEventReqVO kafkaEventReqVO : kafkaEventReqVOList) {
                String kafkaTargetField = kafkaEventReqVO.getTargetField();
                if (!Objects.equals(ruleTargetField, kafkaTargetField)) {
                    continue;
                }

            }
        }
    }

    /**
     * 验证并中止推送Kafka事件组请求
     * 该方法主要负责验证Kafka事件组请求的合法性和合理性，包括渠道是否合法、事件数据集合是否为空、单次上送数据集合元素个数是否超量
     * 如果验证失败，则抛出异常并中止推送操作
     *
     * @param kafkaEventGroupReqVO Kafka事件组请求对象，包含渠道信息和Kafka事件请求列表
     */
    private void validateAndAbortPush(KafkaEventGroupReqVO kafkaEventGroupReqVO) {
        // 判断渠道是否合法
        String channel = kafkaEventGroupReqVO.getChannel();
        Set<String> validChannels = Arrays.stream(ChannelEnum.values())
                .map(ChannelEnum::getCode)
                .collect(Collectors.toSet()); // 获取所有合法渠道的code
        if (!validChannels.contains(channel)) {
            String fieldName = ReflectUtils.getFieldName(KafkaEventDTO::getChannel);
            String message = String.format("字段 [%s]: 无效的渠道 [%s]", fieldName, channel);
            // 构建错误数据对象，并存入 mongodb
            KafkaEventErrorDO kafkaEventErrorDO = KafkaEventErrorDO.builder()
                    .channel(channel)
                    .level(KafkaEventErrorLevelEnum.MAJOR.getCode())
                    .cause(message)
                    .data(JsonUtils.toJsonString(kafkaEventGroupReqVO))
                    .time(LocalDateTime.now())
                    .build();
            kafkaEventErrorRepository.insert(kafkaEventErrorDO);
            throw ServiceExceptionUtil.exception(message, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
        }

        List<KafkaEventReqVO> kafkaEventReqVOList = kafkaEventGroupReqVO.getKafkaEventReqVOList();

        // 判断事件数据集合是否为空
        if (CollUtil.isEmpty(kafkaEventReqVOList)) {
            String fieldName = ReflectUtils.getFieldName(KafkaEventGroupReqVO::getKafkaEventReqVOList);
            String message = String.format("字段 [%s]: 事件数据集合不能为空", fieldName);
            // 构建错误数据对象，并存入 mongodb
            KafkaEventErrorDO kafkaEventErrorDO = KafkaEventErrorDO.builder()
                    .channel(channel)
                    .level(KafkaEventErrorLevelEnum.MAJOR.getCode())
                    .cause(message)
                    .data(JsonUtils.toJsonString(kafkaEventGroupReqVO))
                    .time(LocalDateTime.now())
                    .build();
            kafkaEventErrorRepository.insert(kafkaEventErrorDO);
            throw ServiceExceptionUtil.exception(message, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
        }

        // 判断单次上送数据集合元素个数超量
        int maxSize = 10;
        if (kafkaEventReqVOList.size() > maxSize) {
            String fieldName = ReflectUtils.getFieldName(KafkaEventGroupReqVO::getKafkaEventReqVOList);
            String message = String.format("字段 [%s]: 元素个数必须小于等于 [%d]", fieldName, maxSize);
            // 构建错误数据对象，并存入 mongodb
            KafkaEventErrorDO kafkaEventErrorDO = KafkaEventErrorDO.builder()
                    .channel(channel)
                    .level(KafkaEventErrorLevelEnum.MAJOR.getCode())
                    .cause(message)
                    .data(JsonUtils.toJsonString(kafkaEventGroupReqVO))
                    .time(LocalDateTime.now())
                    .build();
            kafkaEventErrorRepository.insert(kafkaEventErrorDO);
            throw ServiceExceptionUtil.exception(message, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
        }
    }


    /**
     * 验证并过滤无效数据
     * 此方法遍历给定的Kafka事件请求列表，检查每个事件对象的必要字段是否非空
     * 如果字段为空，则记录错误原因并从原列表中移除该对象
     *
     * @param kafkaEventReqVOList 未验证的Kafka事件请求对象列表
     * @return 包含错误信息的Kafka事件错误响应对象列表
     */
    private List<KafkaEventErrorRespVO> validateAndFilterInvalidData(List<KafkaEventReqVO> kafkaEventReqVOList) {

        List<KafkaEventErrorRespVO> kafkaEventErrorRespVOList = new ArrayList<>();

        int index = 0;
        Iterator<KafkaEventReqVO> iterator = kafkaEventReqVOList.iterator();

        while (iterator.hasNext()) {
            KafkaEventReqVO kafkaEventReqVO = iterator.next();
            List<String> reasons = new ArrayList<>();

            // 效验各字段值是否非空
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getTargetField, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getTargetValue, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getEventField, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getEventValue, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getEventAttrMap, reasons);

            if (!reasons.isEmpty()) {
                KafkaEventErrorRespVO kafkaEventErrorRespVO = KafkaEventErrorRespVO.builder()
                        .kafkaEventReqVO(kafkaEventReqVO)
                        .index(index)
                        .reasons(reasons)
                        .build();
                kafkaEventErrorRespVOList.add(kafkaEventErrorRespVO);

                // 移除字段值为空的对象
                iterator.remove();
            }
            index++;
        }

        return kafkaEventErrorRespVOList;
    }

    /**
     * 校验eventTimestamp字段值是否合法
     */
    private <T> void checkEventTimestamp(KafkaEventDTO kafkaEventDTO, SFunction<T> getter, List<String> reasons) {
        try {
            Field field = ReflectUtils.findField(getter);
            Long value = (Long) field.get(kafkaEventDTO);
            if (value == null || String.valueOf(value).length() != 13) {
                reasons.add("[" + field.getName() + "]必须为13位毫秒级别时间戳");
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 检查指定字段值是否为空，添加错误信息
     */
    private <T> void checkNotEmpty(KafkaEventReqVO kafkaEventReqVO, SFunction<T> getter, List<String> reasons) {
        String fieldName = ReflectUtils.getFieldName(getter);
        Object fieldValue = ReflectUtils.getFieldValue(kafkaEventReqVO, getter);
        if (fieldValue == null || (fieldValue instanceof String && !StringUtils.hasText((String) fieldValue))) {
            reasons.add("字段 [" + fieldName + "]: 必须非空");
        }
    }

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    @Override
    public KafkaInfoRespVO kafkaInfo() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        List<String> brokerList = new ArrayList<>();
        List<String> topicList = new ArrayList<>();
        List<String> consumerGroupList = new ArrayList<>();

        try (AdminClient adminClient = AdminClient.create(props)) {
            // 获取集群信息
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            String clusterId = clusterResult.clusterId().get();
            Collection<Node> nodes = clusterResult.nodes().get();
            log.info("Connected to Kafka cluster, Cluster ID: {}, Number of nodes: {}", clusterId, nodes.size());

            for (Node node : nodes) {
                String brokerInfo = String.format("Node ID: %d, Host: %s, Port: %d", node.id(), node.host(), node.port());
                log.info(brokerInfo);
                brokerList.add(brokerInfo);
            }

            // 获取topic列表
            ListTopicsResult topicsResult = adminClient.listTopics();
            Collection<TopicListing> topics = topicsResult.listings().get();
            log.info("Found {} topics:", topics.size());
            for (TopicListing topic : topics) {
                log.info("Topic name: {}", topic.name());
                topicList.add(topic.name());
            }

            // 获取消费组列表
            ListConsumerGroupsResult consumerGroupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> consumerGroups = consumerGroupsResult.all().get();
            log.info("Found {} consumer groups:", consumerGroups.size());
            for (ConsumerGroupListing consumerGroup : consumerGroups) {
                log.info("Consumer group ID: {}", consumerGroup.groupId());
                consumerGroupList.add(consumerGroup.groupId());
            }

            return new KafkaInfoRespVO(bootstrapServers, true, null, brokerList, topicList, consumerGroupList);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to connect to Kafka cluster", e);
            return new KafkaInfoRespVO(bootstrapServers, false, e.getMessage(), null, null, null);
        }
    }

}
