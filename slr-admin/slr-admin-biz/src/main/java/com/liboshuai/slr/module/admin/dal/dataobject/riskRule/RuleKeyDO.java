package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则目标表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_key")
@EqualsAndHashCode(callSuper = true)
public class RuleKeyDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
    /**
     * 目标编号
     * （例如：GAME_userId）
     */
    private String targetCode;
    /**
     * 目标名称
     * （例如：用户id）
     */
    private String targetName;
    /**
     * key描述
     */
    private String targetDesc;
}
