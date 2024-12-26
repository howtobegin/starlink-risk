package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import com.liboshuai.slr.module.engine.enums.RuleCondTypeEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 风控规则条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_cond")
@EqualsAndHashCode(callSuper = true)
public class RuleCondDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 规则编号
     * {@link RuleInfoDO#getRuleCode()}
     */
    private String ruleCode;
    /**
     * 事件编号
     * {@link RuleEventDO#getEventCode()}
     */
    private String eventCode;
    /**
     * 条件编号
     */
    private String condCode;
    /**
     * 条件类型
     * {@link RuleCondTypeEnum}
     */
    private String condType;
    /**
     * 窗口值
     * （仅在条件类型为周期时使用）
     */
    private Long windowValue;
    /**
     * 窗口单位
     * （仅在条件类型为周期时使用）
     * {@link TimeUnitEnum}
     */
    private String windowUnit;
    /**
     * 开始时间
     * （格式：yyyy-MM-dd HH:mm:ss，仅在条件类型为范围时使用）
     */
    private LocalDateTime beginTime;
    /**
     * 结束时间
     * （格式：yyyy-MM-dd HH:mm:ss，仅在条件类型为范围时使用）
     */
    private LocalDateTime endTime;
    /**
     * 阈值
     */
    private Long threshold;
    /**
     * 是否跨历史数据
     */
    private Boolean crossHistory;
    /**
     * 跨历史时间点
     * （格式：yyyy-MM-dd HH:mm:ss）
     */
    private LocalDateTime crossHistoryTimeline;

}
