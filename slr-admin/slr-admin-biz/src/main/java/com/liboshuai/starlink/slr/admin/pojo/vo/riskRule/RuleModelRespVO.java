package com.liboshuai.starlink.slr.admin.pojo.vo.riskRule;

import com.liboshuai.starlink.slr.engine.api.enums.RuleStatusEnum;
import com.liboshuai.starlink.slr.framework.common.pojo.BaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 规则模型信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleModelRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "模型编号", example = "M175928847299117065")
    private String modelCode;

    @Schema(description = "模型名称", example = "模型运算机一")
    private String modelName;

    @Schema(description = "模型描述", example = "支持周期条件的规则模型")
    private String modelDesc;
    /**
     * {@link RuleStatusEnum}
     */
    @Schema(description = "模型状态", example = "支持周期条件的规则模型")
    private String modelStatus;

    @Schema(description = "运算机 groovy 代码")
    private String groovy;
}
