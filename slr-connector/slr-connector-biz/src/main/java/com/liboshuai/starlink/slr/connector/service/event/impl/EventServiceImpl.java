package com.liboshuai.starlink.slr.connector.service.event.impl;

import com.liboshuai.starlink.slr.connector.dao.kafka.provider.EventProvider;
import com.liboshuai.starlink.slr.connector.pojo.vo.event.KafkaInfoVO;
import com.liboshuai.starlink.slr.connector.service.event.EventService;
import com.liboshuai.starlink.slr.connector.service.event.strategy.EventStrategyHolder;
import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    @Resource
    private EventProvider eventProvider;

    @Resource
    private EventStrategyHolder eventStrategyHolder;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    @Override
    public KafkaInfoVO kafkaInfo() {
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

            return new KafkaInfoVO(bootstrapServers, true, null, brokerList, topicList, consumerGroupList);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to connect to Kafka cluster", e);
            return new KafkaInfoVO(bootstrapServers, false, e.getMessage(), null, null, null);
        }
    }

    /**
     * 上送事件数据到kafka
     */
    @Override
    public void uploadKafka(KafkaEventDTO kafkaEventDTO) {
//        String channel = eventUploadDTO.getChannel(); // 渠道
//        List<EventDetailDTO> eventDetailDTOList = eventUploadDTO.getEventDetailDTOList(); // 上送事件详情集合
//
//        // 初步检验上送事件数据参数
//        validateUploadList(eventUploadDTO);
//
//        // 检查并过滤非法数据
//        checkAndFilter(eventDetailDTOList);
//
//        // 各渠道特别的数据处理逻辑
//        EventStrategy eventStrategy = eventStrategyHolder.getByChannel(channel);
//        eventStrategy.processAfter(eventDetailDTOList);
//
//        // 对象转换
//        List<EventKafkaDTO> eventKafkaDTOList = covert(channel, eventDetailDTOList);
//
//        // 异步推送数据到kafka
//        eventProvider.batchSend(eventKafkaDTOList);
    }

    @Override
    public void mockEventToKafka(List<KafkaEventDTO> kafkaEventDTOList) {
        for (KafkaEventDTO kafkaEventDTO : kafkaEventDTOList) {
            eventProvider.mockEventToKafka(kafkaEventDTO);
        }
    }

//    /**
//     * 对象转换
//     */
//    private List<EventKafkaDTO> covert(String channel, List<EventDetailDTO> eventDetailDTOList) {
//        if (CollectionUtils.isEmpty(eventDetailDTOList)) {
//            return new ArrayList<>();
//        }
//        List<EventKafkaDTO> eventKafkaDTOList = EventConvert.INSTANCE.batchDetailToKafkaDTO(eventDetailDTOList);
//        eventKafkaDTOList = eventKafkaDTOList.stream()
//                .peek(eventKafkaDTO -> eventKafkaDTO.setChannel(channel)).collect(Collectors.toList());
//        return eventKafkaDTOList;
//    }
//
//    /**
//     * 初步检验上送事件数据参数
//     */
//    private void validateUploadList(EventUploadDTO eventUploadDTO) {
//        // 判断渠道是否合法
//        String channel = eventUploadDTO.getChannel();
//        Set<String> validChannels = Arrays.stream(ChannelEnum.values())
//                .map(ChannelEnum::getCode)
//                .collect(Collectors.toSet()); // 获取所有合法渠道的code
//        if (!validChannels.contains(channel)) {
//            String fieldName = ReflectUtils.getFieldName(EventUploadDTO::getChannel);
//            String message = String.format("字段 [%s]: 无效的渠道 [%s]", fieldName, channel);
//            EventErrorDTO eventErrorDTO = EventErrorDTO.builder().reasons(Collections.singletonList(message)).build();
//            throw ServiceExceptionUtil.exception(eventErrorDTO, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
//        }
//
//        List<EventDetailDTO> eventDetailDTOList = eventUploadDTO.getEventDetailDTOList();
//
//        // 判断事件数据集合是否为空
//        if (CollUtil.isEmpty(eventDetailDTOList)) {
//            String fieldName = ReflectUtils.getFieldName(EventUploadDTO::getEventDetailDTOList);
//            String message = String.format("字段 [%s]: 事件数据集合不能为空", fieldName);
//            EventErrorDTO eventErrorDTO = EventErrorDTO.builder().reasons(Collections.singletonList(message)).build();
//            throw ServiceExceptionUtil.exception(eventErrorDTO, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
//        }
//
//        // 判断单次上送数据集合元素个数超量
//        int maxSize = 100;
//        if (eventDetailDTOList.size() > maxSize) {
//            String fieldName = ReflectUtils.getFieldName(EventUploadDTO::getEventDetailDTOList);
//            String message = String.format("字段 [%s]: 元素个数必须小于等于 [%d]", fieldName, maxSize);
//            EventErrorDTO eventErrorDTO = EventErrorDTO.builder().reasons(Collections.singletonList(message)).build();
//            throw ServiceExceptionUtil.exception(eventErrorDTO, ErrorCodeConstants.UPLOAD_EVENT_MAJOR_ERROR);
//        }
//    }
//
//
//    /**
//     * 检查并过滤非法数据
//     */
//    private void checkAndFilter(List<EventDetailDTO> eventDetailDTOList) {
//
//        List<EventErrorDTO> eventErrorDTOList = new ArrayList<>();;
//
//        int index = 0;
//        Iterator<EventDetailDTO> iterator = eventDetailDTOList.iterator();
//
//        while (iterator.hasNext()) {
//            EventDetailDTO eventDetailDTO = iterator.next();
//            List<String> reasons = new ArrayList<>();
//
//            // 效验各字段值是否非空
//            checkNotEmpty(eventDetailDTO, EventDetailDTO::getUserCode, reasons);
//            checkNotEmpty(eventDetailDTO, EventDetailDTO::getUsername, reasons);
//            checkNotEmpty(eventDetailDTO, EventDetailDTO::getEventCode, reasons);
//            checkNotEmpty(eventDetailDTO, EventDetailDTO::getEventValue, reasons);
//            checkNotEmpty(eventDetailDTO, EventDetailDTO::getEventTimestamp, reasons);
//
//            // 校验eventTimestamp字段值是否合法
//            checkEventTimestamp(eventDetailDTO, EventDetailDTO::getEventTimestamp, reasons);
//
//            if (!reasons.isEmpty()) {
//                EventErrorDTO eventErrorDTO = EventErrorDTO.builder()
//                        .eventKafkaDTO(eventDetailDTO)
//                        .index(index)
//                        .reasons(reasons)
//                        .build();
//                eventErrorDTOList.add(eventErrorDTO);
//
//                // 移除字段值为空的对象
//                iterator.remove();
//            }
//            index++;
//        }
//
//        if (!eventErrorDTOList.isEmpty()) {
//            throw ServiceExceptionUtil.exception(eventErrorDTOList, ErrorCodeConstants.UPLOAD_EVENT_MINOR_ERROR);
//        }
//    }
//
//    /**
//     * 校验eventTimestamp字段值是否合法
//     */
//    private <T> void checkEventTimestamp(EventDetailDTO eventDetailDTO, SFunction<T> getter, List<String> reasons) {
//        try {
//            Field field = ReflectUtils.findField(getter);
//            Long value = (Long) field.get(eventDetailDTO);
//            if (value == null || String.valueOf(value).length() != 13) {
//                reasons.add("[" + field.getName() + "]必须为13位毫秒级别时间戳");
//            }
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//
//    /**
//     * 检查指定字段值是否为空，添加错误信息
//     */
//    private <T> void checkNotEmpty(EventDetailDTO eventDetailDTO, SFunction<T> getter, List<String> reasons) {
//        String fieldName = ReflectUtils.getFieldName(getter);
//        Object fieldValue = ReflectUtils.getFieldValue(eventDetailDTO, getter);
//        if (fieldValue == null || (fieldValue instanceof String && !StringUtils.hasText((String) fieldValue))) {
//            reasons.add("[" + fieldName+ "]必须非空");
//        }
//    }

}
