package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.enums.RuleEventAttrOpEnum;
import com.liboshuai.slr.engine.api.enums.RuleEventAttrTypeEnum;
import com.liboshuai.slr.engine.api.type.RuleEventAttrValueDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;

/**
 * 风控规则事件属性值
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(RuleEventAttrValueDtoType.class)
public class RuleEventAttrValueDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 属性编号
     * （例如：game_userId_lottery_campaignId）
     */
    private String attrCode;

    /**
     * 属性字段
     * （例如：campaignId）
     */
    private String attrField;

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
     * （例如：R1553673459123456000_game_userId_lottery）
     * {@link RuleCondDTO#getCondCode()}
     */
    private String condCode;
}
