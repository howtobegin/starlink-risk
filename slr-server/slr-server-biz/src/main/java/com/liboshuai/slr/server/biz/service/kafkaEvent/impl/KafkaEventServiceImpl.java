package com.liboshuai.slr.server.biz.service.kafkaEvent.impl;

import cn.hutool.core.collection.CollUtil;
import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.object.reflect.ReflectUtils;
import com.liboshuai.slr.framework.common.util.object.reflect.SFunction;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdGenerator;
import com.liboshuai.slr.server.api.constants.ErrorCodeConstants;
import com.liboshuai.slr.server.api.enums.KafkaEventErrorLevelEnum;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaEventErrorRespVO;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaEventReqVO;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaInfoRespVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleEventAttrRespVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleTargetRespVO;
import com.liboshuai.slr.server.biz.convert.kafkaEvent.KafkaEventConvert;
import com.liboshuai.slr.server.biz.dal.dataobject.kafkaEvent.KafkaEventDO;
import com.liboshuai.slr.server.biz.dal.dataobject.kafkaEvent.KafkaEventErrorDO;
import com.liboshuai.slr.server.biz.dal.kafka.provider.KafkaEventProvider;
import com.liboshuai.slr.server.biz.dal.mongo.KafkaEventErrorRepository;
import com.liboshuai.slr.server.biz.dal.mongo.KafkaEventRepository;
import com.liboshuai.slr.server.biz.framework.properties.SlrServerProperties;
import com.liboshuai.slr.server.biz.service.kafkaEvent.KafkaEventService;
import com.liboshuai.slr.server.biz.service.kafkaEvent.MongoEventService;
import com.liboshuai.slr.server.biz.service.kafkaEvent.strategy.KafkaEventStrategy;
import com.liboshuai.slr.server.biz.service.kafkaEvent.strategy.KafkaEventStrategyHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventServiceImpl implements KafkaEventService {

    private final KafkaEventConvert kafkaEventConvert;
    private final KafkaEventProvider kafkaEventProvider;
    private final KafkaEventStrategyHolder kafkaEventStrategyHolder;
    private final KafkaEventErrorRepository kafkaEventErrorRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final KafkaEventRepository kafkaEventRepository;
    private final MongoEventService mongoEventService;
    private final SlrServerProperties slrServerProperties;


    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    @Override
    public KafkaInfoRespVO kafkaInfo() {
        String bootstrapServers = slrServerProperties.getBootstrapServers();
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

    @Override
    public void createKafkaTopic(String bootstrapServers, String topicName, Integer partition, Short replica) {
        // 配置 AdminClient 属性
        Map<String, Object> config = Collections.singletonMap(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
        );
        // 创建 AdminClient
        try (AdminClient adminClient = AdminClient.create(config)) {
            // 定义新主题
            NewTopic topic = new NewTopic(topicName, partition, replica);
            // 创建主题（异步）
            adminClient.createTopics(Collections.singletonList(topic)).all().get();
            log.info("Kafka Topic 创建成功, 名称: {}, 分区数: {}, 副本数: {}", topicName, partition, replica);
        } catch (Exception e) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.KAFKA_TOPIC_CREATE_ERROR, topicName);
        }
    }

    /**
     * 业务平台上送事件数据到 kafka
     */
    @Override
    public void push(KafkaEventGroupReqVO kafkaEventGroupReqVO) {
//        log.info("业务平台上送事件数据: {}", kafkaEventGroupReqVO);
        String channel = kafkaEventGroupReqVO.getChannel(); // 渠道
        List<KafkaEventReqVO> kafkaEventReqVOList = kafkaEventGroupReqVO.getKafkaEventGroup(); // 上送事件详情集合
        // 若存在非法数据，直接中止推送操作
        validateAndAbortPush(kafkaEventGroupReqVO);
        // 只过滤掉非法数据继续推荐，并给出非法数据错误原因
        List<KafkaEventErrorRespVO> kafkaEventErrorRespVOList = validateAndFilterInvalidData(kafkaEventReqVOList);
        // 验证 KafkaEventGroupReqVO 是否符合规则库中的规则
        validateKafkaEventGroupReqVO(kafkaEventGroupReqVO, kafkaEventErrorRespVOList);
        // req转dto
        List<FlinkEventDTO> flinkEventDTOList = kafkaEventReqVOList.stream()
                .map(kafkaEventConvert::convertReq2Dto) // 转换为DTO
                .map(kafkaEventDTO -> kafkaEventDTO.setChannel(channel)) // 设置渠道
                .collect(Collectors.toList());
        // 各渠道特别的数据处理逻辑
        KafkaEventStrategy kafkaEventStrategy = kafkaEventStrategyHolder.getByChannel(channel);
        kafkaEventStrategy.processAfter(flinkEventDTOList);
        // 生成事件id
        flinkEventDTOList.forEach(kafkaEventDTO -> kafkaEventDTO.setEventId(snowflakeIdGenerator.nextId()));
        // 异步推送数据到kafka
        kafkaEventProvider.batchSend(flinkEventDTOList);
        // 异步保存事件数据到mongo
        mongoEventService.batchSaveEventToMongo(flinkEventDTOList);
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

    @Override
    public List<FlinkEventDTO> selectListByEventIds(List<Long> eventIdList) {
        List<KafkaEventDO> kafkaEventDOList = kafkaEventRepository.findAllByEventIdIn(eventIdList);
        return kafkaEventConvert.batchConvertDo2Dto(kafkaEventDOList);
    }

    /**
     * 验证 KafkaEventGroupReqVO 是否符合规则库中的规则
     *
     * @param kafkaEventGroupReqVO 业务平台上送的 KafkaEventGroupReqVO
     */
    public void validateKafkaEventGroupReqVO(KafkaEventGroupReqVO kafkaEventGroupReqVO,
                                             List<KafkaEventErrorRespVO> kafkaEventErrorRespVOList) {
        String channel = kafkaEventGroupReqVO.getChannel();
//        List<RuleTargetRespVO> ruleTargetRespVOList = ruleTargetService.getCacheDetailList();
        List<RuleTargetRespVO> ruleTargetRespVOList = new ArrayList<>();

        // 如果规则库为空，记录错误并抛出异常
        if (CollectionUtils.isEmpty(ruleTargetRespVOList)) {
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
        Map<String, Map<String, RuleTargetRespVO>> ruleMap = ruleTargetRespVOList.stream()
                .collect(Collectors.groupingBy(RuleTargetRespVO::getChannel,
                        Collectors.toMap(RuleTargetRespVO::getTargetField, ruleTargetDTO -> ruleTargetDTO)));

        // 获取对应 channel 的规则
        Map<String, RuleTargetRespVO> targetFieldMap = ruleMap.get(channel);
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

        List<KafkaEventReqVO> eventReqVOList = kafkaEventGroupReqVO.getKafkaEventGroup();

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
            RuleTargetRespVO ruleTarget = targetFieldMap.get(targetField);
            if (ruleTarget == null) {
                String message = String.format("渠道 [%s] 中的目标 [%s] 在规则库中不存在或未上线", channel, targetField);
                reasons.add(message);
            } else {
                // 查找对应的事件规则
                List<RuleEventRespVO> ruleEventGroup = ruleTarget.getRuleEventGroup();
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

                Optional<RuleEventRespVO> matchingRuleEventOpt = ruleEventGroup.stream()
                        .filter(ruleEventRespVO -> ruleEventRespVO.getEventField().equals(eventField))
                        .findFirst();

                if (!matchingRuleEventOpt.isPresent()) {
                    String message = String.format("渠道 [%s] 目标 [%s] 中的事件 [%s] 在规则库中不存在或未上线",
                            channel, targetField, eventField);
                    reasons.add(message);
                } else {
                    RuleEventRespVO ruleEvent = matchingRuleEventOpt.get();

                    // 获取规则中的属性字段
                    List<RuleEventAttrRespVO> ruleEventAttrGroup = ruleEvent.getRuleEventAttrGroup();
                    if (CollectionUtils.isEmpty(ruleEventAttrGroup)) {
                        // 如果规则中没有属性字段，继续下一个事件
                        index++;
                        continue;
                    }
                    Set<String> ruleAttrFields = ruleEventAttrGroup.stream()
                            .map(RuleEventAttrRespVO::getAttrField)
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
            String fieldName = ReflectUtils.getFieldName(FlinkEventDTO::getChannel);
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

        List<KafkaEventReqVO> kafkaEventReqVOList = kafkaEventGroupReqVO.getKafkaEventGroup();

        // 判断事件数据集合是否为空
        if (CollUtil.isEmpty(kafkaEventReqVOList)) {
            String fieldName = ReflectUtils.getFieldName(KafkaEventGroupReqVO::getKafkaEventGroup);
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
            String fieldName = ReflectUtils.getFieldName(KafkaEventGroupReqVO::getKafkaEventGroup);
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
            checkEventTimestamp(kafkaEventReqVO, KafkaEventReqVO::getEventTime, reasons);
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
    private <T> void checkEventTimestamp(KafkaEventReqVO kafkaEventReqVO, SFunction<T> getter, List<String> reasons) {
        try {
            Field field = ReflectUtils.findField(getter);
            Long value = (Long) field.get(kafkaEventReqVO);
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

    @Override
    public void deleteOldEventFromMongo() {
        // 删除一天前的数据
        long currentTimeMillis = System.currentTimeMillis();
        long oneDayAgo = currentTimeMillis - TimeUnit.DAYS.toMillis(1);
        kafkaEventRepository.deleteByEventTimeBefore(oneDayAgo);
    }

}
