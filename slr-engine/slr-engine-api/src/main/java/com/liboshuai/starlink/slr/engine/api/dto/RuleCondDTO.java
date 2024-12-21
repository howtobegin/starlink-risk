package com.liboshuai.starlink.slr.engine.api.dto;

import com.liboshuai.starlink.slr.engine.api.enums.RuleCondTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 规则条件组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleCondDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 规则编号
     */
    private String ruleCode;
    /**
     * 事件编号
     */
    private String eventCode;
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
     */
    private String windowUnit;
    /**
     * 开始时间
     * （格式：yyyy-MM-dd HH:mm:ss，仅在条件类型为范围时使用）
     */
    private String beginTime;
    /**
     * 结束时间
     * （格式：yyyy-MM-dd HH:mm:ss，仅在条件类型为范围时使用）
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
     * 事件信息
     */
    private EventInfoDTO eventInfoDTO;

}
