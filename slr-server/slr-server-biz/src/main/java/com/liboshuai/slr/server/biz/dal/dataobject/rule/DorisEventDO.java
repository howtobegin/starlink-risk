package com.liboshuai.slr.server.biz.dal.dataobject.rule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * kafka事件表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("starlink_risk_event")
public class DorisEventDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    private LocalDateTime eventTime;

    private String channel;

    private String targetField;

    private String targetValue;

    private String eventField;

    private String eventValue;

    private String eventAttrMap;
}
