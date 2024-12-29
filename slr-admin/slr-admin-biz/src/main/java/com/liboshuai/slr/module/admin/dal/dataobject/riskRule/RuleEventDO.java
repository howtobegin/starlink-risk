package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_event")
@EqualsAndHashCode(callSuper = true)
public class RuleEventDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 目标编号
     *（例如：GAME_userId）
     * {@link RuleKeyDO#getTargetCode()}
     */
    private String targetCode;
    /**
     * 事件编号
     * （例如：GAME_userId_lottery)
     */
    private String eventCode;
    /**
     * 事件名称
     */
    private String eventName;
    /**
     * 事件描述
     */
    private String eventDesc;
}
