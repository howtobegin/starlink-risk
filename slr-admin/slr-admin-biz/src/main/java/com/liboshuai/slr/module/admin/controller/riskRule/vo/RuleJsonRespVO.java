package com.liboshuai.slr.module.admin.controller.riskRule.vo;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 规则json字符串DTO对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleJsonRespVO extends BaseRespVO {

    private static final long serialVersionUID = -6940398101611093673L;

    @Schema(description = "规则编号", example = "R175928847299117063")
    private String ruleCode;

    @Schema(description = "规则json数据")
    private String ruleJson;

}
