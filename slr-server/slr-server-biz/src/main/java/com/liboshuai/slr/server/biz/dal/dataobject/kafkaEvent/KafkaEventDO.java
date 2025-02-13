package com.liboshuai.slr.server.biz.dal.dataobject.kafkaEvent;

import com.liboshuai.slr.engine.api.dto.RuleCondDTO;
import com.liboshuai.slr.engine.api.dto.RuleEventAttrValueDTO;
import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;

/**
 * 上送事件Kafka DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "slr_kafka_event") // 指定集合名称
public class KafkaEventDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /**
     * 事件id
     */
    private String eventId;
    /**
     * 事件时间（例如：1736732339769）
     * 13位，毫秒级
     * 第一版本使用flink处理时间，暂不使用业务平传输的时间
     */
    private Long eventTime;
    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
    /**
     * 目标编号
     * （例如：userId）
     * {@link RuleInfoDTO#getTargetField()}
     */
    private String targetField;
    /**
     * 目标值
     * （例如：U127944222222）
     */
    private String targetValue;
    /**
     * 事件编号
     * （例如：lottery）
     * {@link RuleCondDTO#getEventField()}
     */
    private String eventField;
    /**
     * 事件值
     * （例如：1）
     */
    private String eventValue;
    /**
     * 属性
     * key关联{@link RuleEventAttrValueDTO#getAttrField()}
     */
    private Map<String, String> eventAttrMap;
}
