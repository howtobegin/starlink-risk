package com.liboshuai.slr.server.biz.dal.dataobject.rule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalTime;

/**
 * 风控规则条件时间范围 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_time_range")
@EqualsAndHashCode(callSuper = true)
public class RuleCondTimeRangeDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 类型，如 DAILY,WEEKLY,MONTHLY,YEARLY_MONTH,YEARLY_DATE_RANGE
     */
    private String type;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 适用的星期天，如 MONDAY,WEDNESDAY,FRIDAY
     */
    private String daysOfWeek;

    /**
     * 每月适用开始日期
     */
    private Integer startDayOfMonth;

    /**
     * 每月适用结束日期
     */
    private Integer endDayOfMonth;

    /**
     * 每年适用开始月份
     */
    private Integer startMonth;

    /**
     * 每年适用结束月份
     */
    private Integer endMonth;

    /**
     * 每年固定开始日期 (MM-DD)
     */
    private String startYearlyDate;

    /**
     * 每年固定结束日期 (MM-DD)
     */
    private String endYearlyDate;

    /**
     * 条件编号
     */
    private String condCode;
}