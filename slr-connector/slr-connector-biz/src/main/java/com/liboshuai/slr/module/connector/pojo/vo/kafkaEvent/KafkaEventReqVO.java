package com.liboshuai.slr.module.connector.pojo.vo.kafkaEvent;

import com.liboshuai.slr.module.engine.dto.RuleEventAttrDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 上送事件Kafka 入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KafkaEventReqVO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * key编号
     */
    private String keyCode;
    /**
     * key值
     */
    private String keyValue;
    /**
     * 事件编号
     */
    private String eventCode;
    /**
     * 事件值
     */
    private String eventValue;
    /**
     * 属性
     * {@link RuleEventAttrDTO}
     */
    private Map<String, String> eventAttribute;

}
