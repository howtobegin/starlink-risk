package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.framework.common.pojo.BaseDTO;
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
@EqualsAndHashCode(callSuper = true)
public class RuleKeyDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;

    /**
     * key编号
     */
    private String keyCode;

    /**
     * key名称
     */
    private String keyName;

    /**
     * key描述
     */
    private String keyDesc;
}
