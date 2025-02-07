package com.liboshuai.slr.server.biz.controller.riskRule.vo.resp;

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

    /**
     * {@link RuleInfoRespVO#getRuleCode()}
     */
    @Schema(description = "规则编号", example = "1553673459123456000")
    private Long ruleCode;

    @Schema(description = "规则json数据")
    private String ruleJson;

}
