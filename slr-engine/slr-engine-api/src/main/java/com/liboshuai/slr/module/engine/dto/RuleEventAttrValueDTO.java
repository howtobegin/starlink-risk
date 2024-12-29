package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.framework.common.pojo.BaseDTO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件属性值
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrValueDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 属性编号
     * （例如：GAME_userId_lottery_campaignId）
     */
    private String attrCode;

    /**
     * 属性名称
     * （例如：活动id）
     */
    private String attrName;

    /**
     * 属性类型
     * （例如：String）
     * {@link RuleEventAttrTypeEnum}
     */
    private String attrType;

    /**
     * 属性比较符
     * （例如：==）
     * {@link RuleEventAttrOpEnum}
     */
    private String attrOp;

    /**
     * 属性值
     * （例如：C000000001）
     */
    private String attrValue;

    /**
     * 条件编号
     * （例如：R1553673459123456000_GAME_userId_lottery）
     * {@link RuleCondDTO#getCondCode()}
     */
    private String condCode;
}
