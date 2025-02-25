package com.liboshuai.slr.engine.api.dto;


import com.liboshuai.slr.engine.api.enums.RuleCondTypeEnum;
import com.liboshuai.slr.engine.api.enums.TimeUnitEnum;
import com.liboshuai.slr.engine.api.type.RuleCondDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

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
@TypeInfo(RuleCondDtoType.class)
public class RuleCondDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 条件编号
     * （例如：R1553673459123456000_game_userId_lottery）
     */
    private String condCode;

    /**
     * 条件类型
     * {@link RuleCondTypeEnum}
     */
    private String condType;

    /**
     * 窗口值,仅在条件类型为最近时间时使用
     */
    private Long windowValue;

    /**
     * 窗口单位,仅在条件类型为最近时间时使用
     * {@link TimeUnitEnum}
     */
    private String windowUnit;

    /**
     * 时间范围，仅在条件类型为时间范围时使用
     */
    private TimeRangeDTO timeRange;

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
    private String crossHistoryTimeline;

    /**
     * 规则编号
     * （例如：1553673459123456000）
     * {@link RuleInfoDTO#getRuleCode()}
     */
    private Long ruleCode;

    /**
     * 事件编号
     * （例如：game_userId_lottery）
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
