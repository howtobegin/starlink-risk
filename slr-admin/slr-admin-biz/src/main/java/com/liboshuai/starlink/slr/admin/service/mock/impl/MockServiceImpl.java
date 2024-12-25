package com.liboshuai.starlink.slr.admin.service.mock.impl;

import com.liboshuai.starlink.slr.admin.common.component.snowflake.SnowflakeId;
import com.liboshuai.starlink.slr.admin.common.util.mock.MockEventUtils;
import com.liboshuai.starlink.slr.admin.service.mock.MockService;
import com.liboshuai.starlink.slr.framework.takeTime.core.aop.TakeTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MockServiceImpl implements MockService {

    @Resource
    private SnowflakeId snowflakeId;

    /**
     * 生成的文件路径
     */
    @Value("${slr-admin.loadTest.filePath}")
    private String filePath;

    /**
     * 生成的文件名称
     */
    private static final String FILE_NAME = "event.log";

    /**
     * 创建事件数据文件（文件内容为单条上送模式）
     *
     * @param startMillis    开始的时间戳
     * @param durationMillis 时间跨度
     * @param perSecondCount 每秒生成的时间戳数量
     */
    @Override
    @TakeTime
    @Async("adminAsyncExecutor")
    public void createEventFileSingleMode(long startMillis, long durationMillis, int perSecondCount) {
        // 获取时间戳集合
        Stream<Long> timeStampStream = MockEventUtils.generateTimeStampStream(startMillis, durationMillis, perSecondCount);

        // 生成测试数据并写入文件
//        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath + File.separator + FILE_NAME),
//                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//            timeStampStream.forEach(timeStamp -> {
//                EventDetailDTO eventDetailDTO = EventDetailDTO.builder()
//                        .userCode(snowflakeId.nextIdStr())
//                        .username(MockEventUtils.getUserName())
//                        .eventCode(snowflakeId.nextIdStr())
//                        .eventValue("1")
//                        .eventTimestamp(String.valueOf(timeStamp))
//                        .build();
//                List<EventDetailDTO> eventDetailDTOS = Collections.singletonList(eventDetailDTO);
//                EventUploadDTO eventUploadDTO = EventUploadDTO.builder()
//                        .channel(MockEventUtils.getChannel())
//                        .eventDetailDTOList(eventDetailDTOS)
//                        .build();
//                String json = JsonUtils.toJsonString(eventUploadDTO);
//                try {
//                    writer.write(json);
//                    writer.newLine();
//                } catch (IOException e) {
//                    log.error("生成测试数据并写入文件异常: {}", e.getMessage(), e);
//                    throw new RuntimeException(e);
//                }
//            });
//            writer.flush();
//        } catch (IOException e) {
//            log.error("创建 BufferedWriter 出错: {}", e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
    }

    /**
     * 创建事件数据文件（文件内容为批量上送模式）
     * 
     * @param startMillis    开始的时间戳
     * @param durationMillis 时间跨度
     * @param perSecondCount 每秒生成的时间戳数量
     */
    @Override
    @TakeTime
    @Async("adminAsyncExecutor")
    public void createEventFileBatchMode(long startMillis, long durationMillis, int perSecondCount) {
        // 获取时间戳集合
        List<Long> timeStamps = MockEventUtils.generateTimeStampStream(startMillis, durationMillis, perSecondCount)
                .collect(Collectors.toList());

//        // 生成测试数据并写入文件
//        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath + File.separator + FILE_NAME),
//                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//
//            List<EventKafkaDTO> eventKafkaDTOList = new ArrayList<>();
//
//            for (Long timeStamp : timeStamps) {
//                EventKafkaDTO eventKafkaDTO = EventKafkaDTO.builder()
//                        .userCode(snowflakeId.nextIdStr())
//                        .username(MockEventUtils.getUserName())
//                        .eventCode(snowflakeId.nextIdStr())
//                        .eventValue(snowflakeId.nextIdStr())
//                        .eventTimestamp(String.valueOf(timeStamp))
//                        .build();
//                eventDetailDTOS.add(eventDetailDTO);
//
//                // 每50个EventDetailDTO对象创建一个EventUploadDTO对象
//                if (eventDetailDTOS.size() == 50) {
//                    EventUploadDTO eventUploadDTO = EventUploadDTO.builder()
//                            .channel(MockEventUtils.getChannel())
//                            .eventDetailDTOList(new ArrayList<>(eventDetailDTOS))
//                            .build();
//                    String json = JsonUtils.toJsonString(eventUploadDTO);
//                    try {
//                        writer.write(json);
//                        writer.newLine();
//                    } catch (IOException e) {
//                        log.error("生成测试数据并写入文件异常: {}", e.getMessage(), e);
//                        throw new RuntimeException(e);
//                    }
//                    eventDetailDTOS.clear(); // 清空列表，准备存储下一个50个EventDetailDTO
//                }
//            }
//
//            // 如果最后还有未写入的EventDetailDTO对象
//            if (!eventDetailDTOS.isEmpty()) {
//                EventUploadDTO eventUploadDTO = EventUploadDTO.builder()
//                        .channel(MockEventUtils.getChannel())
//                        .eventDetailDTOList(eventDetailDTOS)
//                        .build();
//                String json = JsonUtils.toJsonString(eventUploadDTO);
//                writer.write(json);
//                writer.newLine();
//            }
//
//            writer.flush();
//        } catch (IOException e) {
//            log.error("创建 BufferedWriter 出错: {}", e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
    }
}