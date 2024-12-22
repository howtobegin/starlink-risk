package com.liboshuai.starlink.slr.connector.controller.event;

import com.liboshuai.starlink.slr.connector.pojo.vo.event.KafkaInfoVO;
import com.liboshuai.starlink.slr.connector.service.event.EventService;
import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.starlink.slr.framework.common.pojo.CommonResult;
import com.liboshuai.starlink.slr.framework.protection.ratelimiter.core.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.liboshuai.starlink.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@Tag(name = "事件接口")
@RequestMapping("/event")
public class EventController {

    @Resource
    private EventService eventService;

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    @GetMapping("/kafka-info")
    @Operation(summary = "获取Kafka信息")
    public CommonResult<KafkaInfoVO> getKafkaInfo() {
        KafkaInfoVO kafkaInfoVO = eventService.kafkaInfo();
        return success(kafkaInfoVO);
    }

    @RateLimiter(count = 10000)
    @PostMapping("/upload-kafka")
    @Operation(summary = "上送事件数据到kafka")
    public CommonResult<?> uploadKafka(@RequestBody KafkaEventDTO kafkaEventDTO) {
        eventService.uploadKafka(kafkaEventDTO);
        return success();
    }

    @RateLimiter(count = 10000)
    @PostMapping("/mock-event-to-kafka")
    @Operation(summary = "mock事件数据到kafka")
    public CommonResult<?> mockEventToKafka(@RequestBody List<KafkaEventDTO> kafkaEventDTOList) {
        eventService.mockEventToKafka(kafkaEventDTOList);
        return success();
    }
}
