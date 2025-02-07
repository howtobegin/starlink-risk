package com.liboshuai.slr.server.biz.controller.mock;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdGenerator;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdProperties;
import com.liboshuai.slr.server.biz.service.mock.MockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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


    @GetMapping("/createEventFileBatchMode")
    @Operation(summary = "创建事件数据文件（文件内容为批量上送模式）")
    public CommonResult<String> createEventFileBatchMode(Long totalCount, Long maxEntries) {
        mockService.createEventFileBatchMode(totalCount, maxEntries);
        return CommonResult.success("事件日志文件开始生成，请等待......");
    }

    @GetMapping("/testSnowflakeId")
    @Operation(summary = "测试雪花id")
    public CommonResult<Long> testSnowflakeId() {
        log.info("snowflakeIdProperties: {}", snowflakeIdProperties);
        return CommonResult.success(snowflakeIdGenerator.nextId());
    }
}
