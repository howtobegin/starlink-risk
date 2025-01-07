package com.liboshuai.slr.module.connector.service.mock.impl;

import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.takeTime.core.aop.TakeTime;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventReqVO;
import com.liboshuai.slr.module.connector.service.mock.MockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class MockServiceImpl implements MockService {

    private static final int MAX_ENTRIES = 1000;
    // Predefined bank names and numbers
    private final List<String> bankNames = new ArrayList<>();
    private final List<String> bankNos = new ArrayList<>();
    private final Random random = new Random();
    @Value("${slr-admin.file-path.event-log}")
    private String eventLogFilePath;
    // Counters for unique ID generation
    private long userIdCounter = 1;
    private long campaignIdCounter = 1;
    private long productIdCounter = 1;

    public MockServiceImpl() {
        initializeBankData();
    }

    private void initializeBankData() {
        // Initialize up to 100 bank names and corresponding bank numbers
        for (int i = 1; i <= 100; i++) {
            bankNames.add(getBankName(i));
            bankNos.add(getBankNo(i));
        }
    }

    private String getBankName(int index) {
        // Customize bank names as needed. 示例:
        switch (index % 5) {
            case 1:
                return "邮储银行";
            case 2:
                return "北京银行";
            case 3:
                return "南京银行";
            case 4:
                return "工商银行";
            default:
                return "招商银行";
        }
    }

    private String getBankNo(int index) {
        // Customize bank numbers as needed. 示例:
        int base = 6000;
        return String.valueOf(base + (index % 100));
    }

    @Override
    @TakeTime
    @Async(DefaultConstants.CONNECTOR_ASYNC_EXECUTOR)
    public void createEventFileBatchMode(long totalCount) {
        log.info("开始生成事件文件，目标条数: {}", totalCount);
        long generatedCount = 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(eventLogFilePath, true))) {
            while (generatedCount < totalCount) {
                // 计算当前批次的大小，确保不会超过 totalCount
                int currentBatchSize = (int) Math.min(random.nextInt(10) + 1, totalCount - generatedCount);
                generatedCount += currentBatchSize;

                // 随机选择一个 channel
                String channel = random.nextBoolean() ? "GAME" : "HJF";

                // 生成当前批次的事件列表
                List<KafkaEventReqVO> eventList = new ArrayList<>(currentBatchSize);
                for (int i = 0; i < currentBatchSize; i++) {
                    KafkaEventReqVO event = generateEvent(channel);
                    eventList.add(event);
                }

                // 创建批次对象
                KafkaEventGroupReqVO batch = KafkaEventGroupReqVO.builder()
                        .channel(channel)
                        .kafkaEventReqVOList(eventList)
                        .build();

                // 将批次写入文件
                writeBatchToFile(writer, batch);
            }
            log.info("事件文件生成完成，文件路径: {}", eventLogFilePath);
        } catch (IOException e) {
            log.error("写入事件文件时发生错误", e);
        }
    }

    private KafkaEventReqVO generateEvent(String channel) {
        KafkaEventReqVO.KafkaEventReqVOBuilder builder = KafkaEventReqVO.builder();
        builder.targetField("userId")
                .targetValue(generateUserId())
                .eventValue(String.valueOf(random.nextInt(11))); // 0-10

        Map<String, String> eventAttrMap = new HashMap<>();

        if ("GAME" .equals(channel)) {
            builder.eventField("lottery");
            // Populate eventAttrMap with campaign and bank info
            eventAttrMap.put("campaignId", generateCampaignId());
            eventAttrMap.put("campaignName", generateCampaignName());
        } else if ("HJF" .equals(channel)) {
            // Randomly choose between orderAmount and orderCount
            builder.eventField(random.nextBoolean() ? "orderAmount" : "orderCount");
            // Populate eventAttrMap with product and bank info
            eventAttrMap.put("productId", generateProductId());
            eventAttrMap.put("productName", generateProductName());
        }

        // Common bank info
        int bankIndex = random.nextInt(bankNames.size());
        eventAttrMap.put("bankName", bankNames.get(bankIndex));
        eventAttrMap.put("bankNo", bankNos.get(bankIndex));

        builder.eventAttrMap(eventAttrMap);
        return builder.build();
    }

    private String generateUserId() {
        if (userIdCounter > MAX_ENTRIES) {
            userIdCounter = 1; // Reset或根据需求处理溢出
        }
        return String.format("U%09d", userIdCounter++);
    }

    private String generateCampaignId() {
        if (campaignIdCounter > MAX_ENTRIES) {
            campaignIdCounter = 1;
        }
        return String.format("C%09d", campaignIdCounter++);
    }

    private String generateCampaignName() {
        return "活动" + (campaignIdCounter - 1);
    }

    private String generateProductId() {
        if (productIdCounter > MAX_ENTRIES) {
            productIdCounter = 1;
        }
        return String.format("P%09d", productIdCounter++);
    }

    private String generateProductName() {
        return "产品" + (productIdCounter - 1);
    }

    private void writeBatchToFile(BufferedWriter writer, KafkaEventGroupReqVO batch) throws IOException {
        String json = JsonUtils.toJsonString(batch);
        writer.write(json);
        writer.newLine();
    }
}
