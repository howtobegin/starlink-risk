package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * kafka事件表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("starlink_risk_kafka_event")
public class DorisEventDO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDateTime eventTime;

    private String channel;

    private String targetField;

    private String targetValue;

    private String eventField;

    private String eventValue;

    private String eventAttrMap;
}
