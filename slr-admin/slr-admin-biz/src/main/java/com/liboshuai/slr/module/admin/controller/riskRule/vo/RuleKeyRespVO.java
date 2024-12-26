package com.liboshuai.slr.module.admin.controller.riskRule.vo;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 风控规则目标表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleKeyRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
    /**
     * key编号
     * （例如：GAME_userId）
     */
    private String keyCode;
    /**
     * key名称
     * （例如：用户id）
     */
    private String keyName;
    /**
     * key描述
     */
    private String keyDesc;
}
