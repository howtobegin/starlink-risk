package com.liboshuai.slr.module.admin.controller.riskRule.vo.resp;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 事件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "key编号", example = "GAME_userId")
    private String keyCode;

    @Schema(description = "事件编号", example = "GAME_LOTTERY")
    private String eventCode;

    @Schema(description = "事件名称", example = "游戏抽奖")
    private String eventName;

    @Schema(description = "事件描述", example = "游戏平台的抽奖事件")
    private String eventDesc;

    @Schema(description = "事件属性组")
    private List<RuleEventAttrRespVO> ruleEventAttrRespVoList;
}
