package com.liboshuai.slr.server.biz.controller.rule.vo.resp;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
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
public class RuleModelRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "模型编号", example = "1553673459123456001")
    private Long modelCode;

    @Schema(description = "模型名称", example = "模型运算机一")
    private String modelName;

    @Schema(description = "模型描述", example = "支持周期条件的规则模型")
    private String modelDesc;

    @Schema(description = "运算机groovy代码")
    private String groovy;
}
