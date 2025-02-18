package com.liboshuai.slr.server.biz.service.mock.impl;

import com.liboshuai.slr.engine.api.dto.MockEventDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.takeTime.core.aop.TakeTime;
import com.liboshuai.slr.server.biz.constants.AsyncExecutorConstants;
import com.liboshuai.slr.server.biz.framework.properties.SlrServerProperties;
import com.liboshuai.slr.server.biz.service.mock.MockService;
import com.liboshuai.slr.server.biz.service.mock.NginxService;
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

    private static final int BUFFER_SIZE = 64 * 1024; // 64KB 的 Buffer，提升 I/O 写入效率

    @Resource
    private SlrServerProperties slrServerProperties;
    @Resource
    private NginxService nginxService;

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
        int batchSize = 5000; // 增加批处理大小，减少 I/O 次数

        List<String> urlParamsBatch = new ArrayList<>(batchSize);
        StringBuilder batchBuffer = new StringBuilder(batchSize * 250); // 预分配容量，降低扩容开销

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(slrServerProperties.getEventLogFilePath(), false), BUFFER_SIZE)) {
            while (generatedCount < totalCount) {
                generatedCount++;

                // 生成事件
                String channel = random.nextBoolean() ? "game" : "hjf";
                MockEventDTO mockEventDTO = generateEvent(channel);
                String urlParams = convertToParams(mockEventDTO);

                // 批量收集数据
                urlParamsBatch.add(urlParams);

                // 如果达到批量写入阈值，则一次性写入
                if (urlParamsBatch.size() >= batchSize) {
                    writeBatchToFile(writer, urlParamsBatch, batchBuffer);
                    urlParamsBatch.clear(); // 清空列表
                    batchBuffer.setLength(0); // 清空 StringBuilder
                }
            }

            // 处理剩余数据
            if (!urlParamsBatch.isEmpty()) {
                writeBatchToFile(writer, urlParamsBatch, batchBuffer);
            }
        } catch (IOException e) {
            log.error("写入事件文件时发生错误", e);
        }
    }

    @Override
    public void pushEventToNginx(List<MockEventDTO> mockEventDTOList) {
        for (MockEventDTO mockEventDTO : mockEventDTOList) {
            nginxService.sendEventRequest(mockEventDTO);
        }
    }

    private MockEventDTO generateEvent(String channel) {
        MockEventDTO.MockEventDTOBuilder builder = MockEventDTO.builder();

        Map<String, String> eventAttrMap = new HashMap<>();

        String eventField;
        if ("game".equals(channel)) {
            eventField = "lottery";
            builder.eventField(eventField);
            // Populate eventAttrMap with campaign and bank info
            eventAttrMap.put("campaignId", generateCampaignId());
            eventAttrMap.put("campaignName", generateCampaignName());
        } else if ("hjf".equals(channel)) {
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
    private String convertToParams(MockEventDTO mockEventDTO) throws IOException {
        // 打点服务器的请求 URI 地址（建议动态配置）
        String baseUri = "http://docker:48881/slr_event_" + slrServerProperties.getActive() + ".gif ";

        // 使用 UriComponentsBuilder 构建 URI，并添加查询参数
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUri)
                .queryParam("channel", mockEventDTO.getChannel())
                .queryParam("targetField", mockEventDTO.getTargetField())
                .queryParam("targetValue", mockEventDTO.getTargetValue())
                .queryParam("eventField", mockEventDTO.getEventField())
                .queryParam("eventValue", mockEventDTO.getEventValue())
                .queryParam("eventAttrMap", JsonUtils.toJsonString(mockEventDTO.getEventAttrMap()))
                .build()
                .encode(StandardCharsets.UTF_8); // 让 UriComponentsBuilder 进行 UTF-8 编码
        String[] split = uriComponents.toUriString().split("\\?");
        return split[1];
    }

    /**
     * 一次性写入一批事件到文件（使用 StringBuilder 提高批量写入性能）
     */
    private void writeBatchToFile(BufferedWriter writer, List<String> urlParamsBatch, StringBuilder batchBuffer) throws IOException {
        for (String urlParams : urlParamsBatch) {
            batchBuffer.append(urlParams).append("\n"); // 使用 StringBuilder 避免频繁调用 writer.write()
        }
        writer.write(batchBuffer.toString()); // 一次性写入文件，减少 IO 交互
        writer.flush(); // 强制刷盘，提升数据可靠性
    }

}
