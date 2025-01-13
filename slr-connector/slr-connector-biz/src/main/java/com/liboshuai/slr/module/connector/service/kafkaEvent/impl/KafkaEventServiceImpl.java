package com.liboshuai.slr.module.connector.service.kafkaEvent.impl;

import cn.hutool.core.collection.CollUtil;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.object.reflect.ReflectUtils;
import com.liboshuai.slr.framework.common.util.object.reflect.SFunction;
import com.liboshuai.slr.module.admin.api.riskRule.RuleTargetApi;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleEventAttrDTO;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleEventDTO;
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
        // 验证 KafkaEventGroupReqVO 是否符合规则库中的规则
        validateKafkaEventGroupReqVO(kafkaEventGroupReqVO, kafkaEventErrorRespVOList);
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

    /**
     * 验证 KafkaEventGroupReqVO 是否符合规则库中的规则
     *
     * @param kafkaEventGroupReqVO 业务平台上送的 KafkaEventGroupReqVO
     */
    public void validateKafkaEventGroupReqVO(KafkaEventGroupReqVO kafkaEventGroupReqVO,
                                             List<KafkaEventErrorRespVO> kafkaEventErrorRespVOList) {
        String channel = kafkaEventGroupReqVO.getChannel();
        List<RuleTargetDTO> ruleTargetDTOList = ruleTargetApi.getCacheDetailList();

        // 如果规则库为空，记录错误并抛出异常
        if (CollectionUtils.isEmpty(ruleTargetDTOList)) {
            String message = "因上线的规则目标事件配置项条数为0，故不接受业务平台任何上送数据";
            log.error(message);
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

        // 根据 channel 和 targetField 组织规则库，便于快速查找
        Map<String, Map<String, RuleTargetDTO>> ruleMap = ruleTargetDTOList.stream()
                .collect(Collectors.groupingBy(RuleTargetDTO::getChannel,
                        Collectors.toMap(RuleTargetDTO::getTargetField, ruleTargetDTO -> ruleTargetDTO)));

        // 获取对应 channel 的规则
        Map<String, RuleTargetDTO> targetFieldMap = ruleMap.get(channel);
        if (targetFieldMap == null) {
            String message = String.format("渠道 [%s] 在规则库中不存在或未上线", channel);
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

        List<KafkaEventReqVO> eventReqVOList = kafkaEventGroupReqVO.getKafkaEventReqVOList();

        // 使用 Iterator 安全地遍历并修改列表
        Iterator<KafkaEventReqVO> iterator = eventReqVOList.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            KafkaEventReqVO eventReqVO = iterator.next();
            List<String> reasons = new ArrayList<>();

            String targetField = eventReqVO.getTargetField();
            String eventField = eventReqVO.getEventField();
            Map<String, String> eventAttrMap = eventReqVO.getEventAttrMap();

            // 查找对应的目标规则
            RuleTargetDTO ruleTarget = targetFieldMap.get(targetField);
            if (ruleTarget == null) {
                String message = String.format("渠道 [%s] 中的目标 [%s] 在规则库中不存在或未上线", channel, targetField);
                reasons.add(message);
            } else {
                // 查找对应的事件规则
                List<RuleEventDTO> ruleEventGroup = ruleTarget.getRuleEventGroup();
                if (CollectionUtils.isEmpty(ruleEventGroup)) {
                    // 记录错误原因
                    String message = String.format("渠道 [%s] 目标 [%s] 的规则事件组在规则库中不存在或未上线，无法处理事件字段 [%s]",
                            channel, targetField, eventField);
                    reasons.add(message);
                    // 移除当前事件
                    iterator.remove();
                    // 记录错误响应
                    KafkaEventErrorRespVO errorRespVO = KafkaEventErrorRespVO.builder()
                            .index(index)
                            .reasons(Collections.singletonList(message))
                            .kafkaEventReqVO(eventReqVO)
                            .build();
                    kafkaEventErrorRespVOList.add(errorRespVO);
                    index++;
                    continue;
                }

                Optional<RuleEventDTO> matchingRuleEventOpt = ruleEventGroup.stream()
                        .filter(ruleEventDTO -> ruleEventDTO.getEventField().equals(eventField))
                        .findFirst();

                if (!matchingRuleEventOpt.isPresent()) {
                    String message = String.format("渠道 [%s] 目标 [%s] 中的事件 [%s] 在规则库中不存在或未上线",
                            channel, targetField, eventField);
                    reasons.add(message);
                } else {
                    RuleEventDTO ruleEvent = matchingRuleEventOpt.get();

                    // 获取规则中的属性字段
                    List<RuleEventAttrDTO> ruleEventAttrGroup = ruleEvent.getRuleEventAttrGroup();
                    if (CollectionUtils.isEmpty(ruleEventAttrGroup)) {
                        // 如果规则中没有属性字段，继续下一个事件
                        index++;
                        continue;
                    }
                    Set<String> ruleAttrFields = ruleEventAttrGroup.stream()
                            .map(RuleEventAttrDTO::getAttrField)
                            .collect(Collectors.toSet());

                    if (CollectionUtils.isEmpty(eventAttrMap)) {
                        // 如果事件中没有属性字段，继续下一个事件
                        index++;
                        continue;
                    }
                    // 获取上送的属性字段
                    Set<String> requestAttrFields = eventAttrMap.keySet();

                    // 检查是否存在未在规则库中定义的属性字段
                    Set<String> extraAttrs = new HashSet<>(requestAttrFields);
                    extraAttrs.removeAll(ruleAttrFields);
                    if (!extraAttrs.isEmpty()) {
                        String message = String.format("渠道 [%s] 目标 [%s] 事件 [%s] 中的事件属性中存在规则库中未定义的属性字段 [%s]",
                                channel, targetField, eventField, extraAttrs);
                        reasons.add(message);
                    }
                }
            }

            // 如果存在不合法的原因，记录错误响应并移除事件
            if (!reasons.isEmpty()) {
                KafkaEventErrorRespVO errorRespVO = KafkaEventErrorRespVO.builder()
                        .index(index)
                        .reasons(reasons)
                        .kafkaEventReqVO(eventReqVO)
                        .build();
                kafkaEventErrorRespVOList.add(errorRespVO);
                // 移除当前事件
                iterator.remove();
            }

            index++;
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
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getEventTime, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getTargetField, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getTargetValue, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getEventField, reasons);
            checkNotEmpty(kafkaEventReqVO, KafkaEventReqVO::getEventValue, reasons);

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
