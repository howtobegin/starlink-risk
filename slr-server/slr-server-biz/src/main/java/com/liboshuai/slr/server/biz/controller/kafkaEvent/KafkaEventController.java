package com.liboshuai.slr.server.biz.controller.kafkaEvent;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaInfoRespVO;
import com.liboshuai.slr.server.biz.framework.properties.KafkaProperties;
import com.liboshuai.slr.server.biz.service.kafkaEvent.KafkaEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控事件数据")
@RequestMapping("/kafkaEvent")
public class KafkaEventController {

    private final KafkaEventService kafkaEventService;
    private final KafkaProperties kafkaProperties;


    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    @GetMapping("/getKafkaInfo")
    @Operation(summary = "获取Kafka信息")
    public CommonResult<KafkaInfoRespVO> getKafkaInfo() {
        KafkaInfoRespVO kafkaInfoRespVO = kafkaEventService.kafkaInfo();
        return success(kafkaInfoRespVO);
    }

    /**
     * 创建kafka事件topic
     */
    @GetMapping("/createKafkaEventTopic")
    @Operation(summary = "创建kafka事件topic")
    public CommonResult<Boolean> createKafkaEventTopic() {
        kafkaEventService.createKafkaTopic(
                kafkaProperties.getBootstrapServers(),
                kafkaProperties.getEventTopic(),
                kafkaProperties.getEventPartition(),
                kafkaProperties.getEventReplica()
        );
        return success(true);
    }

    /**
     * 创建kafka预警信息topic
     */
    @GetMapping("/createKafkaAlertTopic")
    @Operation(summary = "创建kafka预警信息topic")
    public CommonResult<Boolean> createKafkaAlertTopic() {
        kafkaEventService.createKafkaTopic(
                kafkaProperties.getBootstrapServers(),
                kafkaProperties.getAlertTopic(),
                kafkaProperties.getAlertPartition(),
                kafkaProperties.getAlertReplica()
        );
        return success(true);
    }

    /**
     * 业务平台上送事件数据到 kafka
     */
    @PostMapping("/push")
    @Operation(summary = "推送")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "事件数据推送请求", required = true,
            content = @Content(schema = @Schema(implementation = KafkaEventGroupReqVO.class)))
    public CommonResult<?> push(@RequestBody @Valid KafkaEventGroupReqVO kafkaEventGroupReqVO) {
        kafkaEventService.push(kafkaEventGroupReqVO);
        return success();
    }
}
