package com.liboshuai.slr.module.connector.controller.kafkaEvent;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.ratelimiter.core.annotation.RateLimiter;
import com.liboshuai.slr.module.connector.pojo.vo.kafkaEvent.KafkaEventGroupReqVO;
import com.liboshuai.slr.module.connector.pojo.vo.kafkaEvent.KafkaInfoRespVO;
import com.liboshuai.slr.module.connector.service.kafkaEvent.KafkaEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@Tag(name = "kafka事件接口")
@RequestMapping("/kafkaEvent")
public class KafkaEventController {

    @Resource
    private KafkaEventService kafkaEventService;

    /**
     * 业务平台上送事件数据到 kafka
     */
    @RateLimiter(count = 10000)
    @PostMapping("/push")
    @Operation(summary = "上送事件数据到kafka")
    public CommonResult<?> push(@RequestBody KafkaEventGroupReqVO kafkaEventGroupReqVO) {
        kafkaEventService.push(kafkaEventGroupReqVO);
        return success();
    }

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    @GetMapping("/getKafkaInfo")
    @Operation(summary = "获取Kafka信息")
    public CommonResult<KafkaInfoRespVO> getKafkaInfo() {
        KafkaInfoRespVO kafkaInfoRespVO = kafkaEventService.kafkaInfo();
        return success(kafkaInfoRespVO);
    }
}
