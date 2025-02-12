package com.liboshuai.slr.server.biz.service.mock.impl;

import com.liboshuai.slr.engine.api.dto.EventDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.takeTime.core.aop.TakeTime;
import com.liboshuai.slr.server.biz.constants.AsyncExecutorConstants;
import com.liboshuai.slr.server.biz.framework.properties.SlrServerProperties;
import com.liboshuai.slr.server.biz.service.mock.MockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class MockServiceImpl implements MockService {

    private static final int MAX_ENTRIES = 1000000;
    // Predefined bank names and numbers
    private final List<String> bankNames = new ArrayList<>();
    private final List<String> bankNos = new ArrayList<>();
    private final Random random = new Random();
    // Counters for unique ID generation
    private long userIdCounter = 1;
    private long campaignIdCounter = 1;
    private long productIdCounter = 1;

    @Resource
    private SlrServerProperties slrServerProperties;

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
    @Async(AsyncExecutorConstants.MOCK_EVENT_FILE_ASYNC_EXECUTOR)
    public void createEventFile(Long totalCount, Long maxEntries) {
        long generatedCount = 0;
        int batchSize = 1000;

        List<String> urlParamsBatch = new ArrayList<>(batchSize);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(slrServerProperties.getEventLogFilePath(), false))) {
            while (generatedCount < totalCount) {
                generatedCount++;

                // 生成事件
                String channel = random.nextBoolean() ? "GAME" : "HJF";
                EventDTO eventDTO = generateEvent(channel);
                String urlParams = convertToParams(eventDTO);

                // 添加到批次列表
                urlParamsBatch.add(urlParams);

                // 如果达到批量阈值，则一次性写入文件
                if (urlParamsBatch.size() >= batchSize) {
                    writeBatchToFile(writer, urlParamsBatch);
                    urlParamsBatch.clear(); // 清空列表，继续收集下一批数据
                }
            }

            // 处理剩余的事件
            if (!urlParamsBatch.isEmpty()) {
                writeBatchToFile(writer, urlParamsBatch);
            }
        } catch (IOException e) {
            log.error("写入事件文件时发生错误", e);
        }
    }

    private EventDTO generateEvent(String channel) {
        EventDTO.EventDTOBuilder builder = EventDTO.builder();

        Map<String, String> eventAttrMap = new HashMap<>();

        String eventField;
        if ("GAME".equals(channel)) {
            eventField = "lottery";
            builder.eventField(eventField);
            // Populate eventAttrMap with campaign and bank info
            eventAttrMap.put("campaignId", generateCampaignId());
            eventAttrMap.put("campaignName", generateCampaignName());
        } else if ("HJF".equals(channel)) {
            // Randomly choose between orderAmount and orderCount
            eventField = random.nextBoolean() ? "orderAmount" : "orderCount";
            builder.eventField(eventField);
            // Populate eventAttrMap with product and bank info
            eventAttrMap.put("productId", generateProductId());
            eventAttrMap.put("productName", generateProductName());
        } else {
            throw new IllegalArgumentException("Invalid channel: " + channel);
        }

        if (Objects.equals(eventField, "orderAmount")) {
            // 订单金额
            builder.eventValue(String.valueOf(random.nextInt(100)));
        }

        // Common bank info
        int bankIndex = random.nextInt(bankNames.size());
        eventAttrMap.put("bankName", bankNames.get(bankIndex));
        eventAttrMap.put("bankNo", bankNos.get(bankIndex));

        builder.eventAttrMap(eventAttrMap);

        builder.channel(channel)
                .targetField("userId")
                .targetValue(generateUserId())
                .eventValue(String.valueOf(random.nextInt(11)));
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

    /**
     * 将 EventDTO 转换为 url参数
     */
    private String convertToParams(EventDTO eventDTO) throws IOException {
        // 打点服务器的请求 URI 地址（建议动态配置）
        String baseUri = "http://docker:48881/slr_" + slrServerProperties.getActive() + ".gif ";

        // 使用 UriComponentsBuilder 构建 URI，并添加查询参数
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUri)
                .queryParam("channel", eventDTO.getChannel())
                .queryParam("targetField", eventDTO.getTargetField())
                .queryParam("targetValue", eventDTO.getTargetValue())
                .queryParam("eventField", eventDTO.getEventField())
                .queryParam("eventValue", eventDTO.getEventValue())
                .queryParam("eventAttrMap", JsonUtils.toJsonString(eventDTO.getEventAttrMap()))
                .build()
                .encode(StandardCharsets.UTF_8); // 让 UriComponentsBuilder 进行 UTF-8 编码
        String[] split = uriComponents.toUriString().split("\\?");
        return split[1];
    }

    /**
     * 一次性写入一批事件到文件
     */
    private void writeBatchToFile(BufferedWriter writer, List<String> urlParamsBatch) throws IOException {
        for (String urlParams : urlParamsBatch) {
            writer.write(urlParams);
            writer.newLine();
        }
        writer.flush(); // 确保数据写入磁盘
    }
}
