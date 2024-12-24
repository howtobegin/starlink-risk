package com.liboshuai.starlink.slr.connector.controller.kafkaEvent;

import com.liboshuai.starlink.slr.connector.pojo.dto.kafkaEvent.KafkaEventGroupDTO;
import com.liboshuai.starlink.slr.connector.pojo.vo.kafkaEvent.KafkaInfoVO;
import com.liboshuai.starlink.slr.connector.service.kafkaEvent.KafkaEventService;
import com.liboshuai.starlink.slr.framework.common.pojo.CommonResult;
import com.liboshuai.starlink.slr.framework.protection.ratelimiter.core.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.liboshuai.starlink.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@Tag(name = "kafka事件接口")
@RequestMapping("/kafkaEvent")
public class KafkaEventController {

    @Resource
    private KafkaEventService kafkaEventService;

    @RateLimiter(count = 10000)
    @PostMapping("/push")
    @Operation(summary = "上送事件数据到kafka")
    public CommonResult<?> push(@RequestBody KafkaEventGroupDTO kafkaEventGroupDTO) {
        kafkaEventService.push(kafkaEventGroupDTO);
        return success();
    }

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    @GetMapping("/getKafkaInfo")
    @Operation(summary = "获取Kafka信息")
    public CommonResult<KafkaInfoVO> getKafkaInfo() {
        KafkaInfoVO kafkaInfoVO = kafkaEventService.kafkaInfo();
        return success(kafkaInfoVO);
    }
}
