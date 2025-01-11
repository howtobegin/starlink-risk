package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * kafka事件表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_kafka_event")
public class KafkaEventDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField("EVENT_TIME")
    private Date eventTime;

    @TableField("channel")
    private String channel;

    @TableField("TARGET_FIELD")
    private String targetField;

    @TableField("TARGET_VALUE")
    private String targetValue;

    @TableField("EVENT_FIELD")
    private String eventField;

    @TableField("EVENT_VALUE")
    private String eventValue;

    @TableField("EVENT_ATTR_MAP")
    private Map<String, String> eventAttrMap;
}
