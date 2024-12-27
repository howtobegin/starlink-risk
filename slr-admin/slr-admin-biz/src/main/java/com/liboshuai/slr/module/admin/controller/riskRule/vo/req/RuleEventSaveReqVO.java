package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "事件编号[eventCode]，不能为空")
    @Schema(description = "事件编号", example = "GAME_LOTTERY")
    private String eventCode;

    @NotNull(message = "事件属性组[ruleEventAttrSaveReqVOList]，不能为空")
    @Schema(description = "事件属性组")
    private List<RuleEventAttrSaveReqVO> ruleEventAttrSaveReqVOList;
}
