package com.liboshuai.slr.server.biz.controller.mock;

import com.liboshuai.slr.engine.api.dto.MockEventDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/pushEventToNginx")
    @Operation(summary = "推送数据到nginx")
    public CommonResult<Boolean> pushEventToNginx(@RequestBody List<MockEventDTO> mockEventDTOList) {
        mockService.pushEventToNginx(mockEventDTOList);
        return CommonResult.success(true);
    }

    @GetMapping("/testNginxBackendRequest")
    @Operation(summary = "测试nginx日志打点")
    public CommonResult<Boolean> testNginxBackendRequest() {
        nginxService.testNginxBackendRequest();
        return CommonResult.success(true);
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

    @GetMapping("/testDom4j")
    @Operation(summary = "测试dom4j")
    public CommonResult<Boolean> testDom4j() {
        mockService.testDom4j();
        return CommonResult.success(true);
    }
}
