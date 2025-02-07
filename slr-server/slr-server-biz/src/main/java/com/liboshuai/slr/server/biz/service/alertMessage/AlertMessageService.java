package com.liboshuai.slr.server.biz.service.alertMessage;

import com.liboshuai.slr.engine.api.dto.AlertMessageDTO;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessagePageReqVO;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessageRespVO;

import java.util.List;

public interface AlertMessageService {
    /**
     * 预警信息 分页查询
     */
    PageResult<AlertMessageRespVO> page(AlertMessagePageReqVO alertMessagePageReqVO);

    /**
     * 根据规则编号查询预警信息
     *
     * @param ruleCode 规则编号
     * @return 预警信息集合
     */
    List<AlertMessageDTO> findByRuleCode(Long ruleCode);
}
