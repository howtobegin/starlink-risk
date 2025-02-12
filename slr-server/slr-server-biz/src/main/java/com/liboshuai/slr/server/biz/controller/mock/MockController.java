package com.liboshuai.slr.server.biz.controller.mock;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdGenerator;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdProperties;
import com.liboshuai.slr.server.biz.service.mock.MockService;
import com.liboshuai.slr.server.biz.service.mock.NginxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "mock数据测试")
@RequestMapping("/mock")
public class MockController {

    private final MockService mockService;
    private final SnowflakeIdProperties snowflakeIdProperties;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final NginxService nginxService;
    private final RedisTemplate<String, Object> redisTemplate;


    @GetMapping("/createEventFile")
    @Operation(summary = "创建事件数据文件")
    public CommonResult<String> createEventFile(Long totalCount, Long maxEntries) {
        mockService.createEventFile(totalCount, maxEntries);
        return CommonResult.success("事件日志文件开始生成，请等待......");
    }

    @GetMapping("/testSnowflakeId")
    @Operation(summary = "测试雪花id")
    public CommonResult<Long> testSnowflakeId() {
        log.info("snowflakeIdProperties: {}", snowflakeIdProperties);
        return CommonResult.success(snowflakeIdGenerator.nextId());
    }

    @GetMapping("/testNginxBackendRequest")
    @Operation(summary = "测试nginx日志打点")
    public CommonResult<Boolean> testNginxBackendRequest() {
        nginxService.testNginxBackendRequest();
        return CommonResult.success(true);
    }

    @GetMapping("/backend")
    public String handleBackendGif(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String targetField,
            @RequestParam(required = false) String targetValue,
            @RequestParam(required = false) String eventField,
            @RequestParam(required = false) String eventValue,
            @RequestParam(required = false) String eventAttrMap
    ) {
        System.out.println("Received parameters:");
        System.out.println("channel: " + channel);
        System.out.println("targetField: " + targetField);
        System.out.println("targetValue: " + targetValue);
        System.out.println("eventField: " + eventField);
        System.out.println("eventValue: " + eventValue);
        System.out.println("eventAttrMap: " + eventAttrMap);

        // 您可以根据需要进行进一步处理

        return "Parameters received successfully.";
    }

    @GetMapping("/testRedis")
    @Operation(summary = "测试redis")
    public CommonResult<Boolean> testRedis() {
        redisTemplate.opsForValue().set("name", "lbs");
        Object name = redisTemplate.opsForValue().get("name");
        log.info("name: {}", name);

        redisTemplate.opsForHash().put("dianzi", "phone", "苹果");
        redisTemplate.opsForHash().put("dianzi", "book", "小米");
        Object phone = redisTemplate.opsForHash().get("dianzi", "phone");
        Object book = redisTemplate.opsForHash().get("dianzi", "book");
        log.info("phone: {}", phone);
        log.info("book: {}", book);
        return CommonResult.success(true);
    }
}
