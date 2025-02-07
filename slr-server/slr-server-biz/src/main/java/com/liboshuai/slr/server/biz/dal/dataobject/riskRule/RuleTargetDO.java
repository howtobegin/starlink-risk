package com.liboshuai.slr.server.biz.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
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
@TableName("slr_rule_target")
@EqualsAndHashCode(callSuper = true)
public class RuleTargetDO extends BaseDO {
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
     * 目标字段
     * （例如：userId）
     */
    private String targetField;
    /**
     * 目标名称
     * （例如：用户id）
     */
    private String targetName;
    /**
     * 目标描述
     */
    private String targetDesc;
}
