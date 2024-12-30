package com.liboshuai.slr.module.engine.dto;


import com.liboshuai.slr.module.engine.enums.RuleCondTypeEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 规则条件组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleCondDTO implements Serializable {
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
     * 窗口值,仅在条件类型为周期时使用
     */
    private Long windowValue;

    /**
     * 窗口单位,仅在条件类型为周期时使用
     * {@link TimeUnitEnum}
     */
    private String windowUnit;

    /**
     * 开始时间,仅在条件类型为范围时使用
     * （格式：yyyy-MM-dd HH:mm:ss）
     */
    private String beginTime;

    /**
     * 结束时间,仅在条件类型为范围时使用
     * （格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

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
    private String crossHistoryTimeline;

    /**
     * 规则编号
     * （例如：R1553673459123456000）
     * {@link RuleInfoDTO#getRuleCode()}
     */
    private String ruleCode;

    /**
     * 事件编号
     * （例如：GAME_userId_lottery）
     */
    private String eventCode;

    /**
     * 事件字段
     * （例如：lottery）
     */
    private String eventField;

    /**
     * 事件名称
     * （例如：游戏抽奖）
     */
    private String eventName;

    /**
     * 事件属性值组
     */
    private List<RuleEventAttrValueDTO> ruleEventAttrValueGroup;

}
