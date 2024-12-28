package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleModelPageReqVO extends PageParam {
    private static final long serialVersionUID = 1L;

    @Schema(description = "模型编号", example = "M175928847299117065")
    private String modelCode;

    @Schema(description = "模型名称", example = "模型运算机一")
    private String modelName;

    @Schema(description = "模型描述", example = "支持周期条件的规则模型")
    private String modelDesc;
}
