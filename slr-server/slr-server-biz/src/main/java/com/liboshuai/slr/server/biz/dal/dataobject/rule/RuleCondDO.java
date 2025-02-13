package com.liboshuai.slr.server.biz.dal.dataobject.rule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.engine.api.enums.RuleCondTypeEnum;
import com.liboshuai.slr.engine.api.enums.TimeUnitEnum;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
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
     * 条件编号
     * （例如：R1553673459123456000_GAME_userId_lottery）
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
     * 阈值缩放因子
     */
    private Long thresholdScaleFactor;
    /**
     * 是否跨历史数据
     */
    private Boolean crossHistory;
    /**
     * 跨历史时间点
     * （格式：yyyy-MM-dd HH:mm:ss）
     */
    private LocalDateTime crossHistoryTimeline;
    /**
     * 规则编号
     * （例如：R1553673459123456000）
     * {@link RuleInfoDO#getRuleCode()}
     */
    private Long ruleCode;
    /**
     * 事件编号
     * （例如：GAME_userId_lottery）
     * {@link RuleEventDO#getEventCode()}
     */
    private String eventCode;

}
