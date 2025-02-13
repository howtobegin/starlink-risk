package com.liboshuai.slr.server.biz.service.alertMessage;

import com.liboshuai.slr.engine.api.dto.AlertDTO;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.server.biz.controller.alert.vo.AlertPageReqVO;
import com.liboshuai.slr.server.biz.controller.alert.vo.AlertRespVO;

import java.util.List;

public interface AlertMessageService {
    /**
     * 预警信息 分页查询
     */
    PageResult<AlertRespVO> page(AlertPageReqVO alertPageReqVO);

    /**
     * 根据规则编号查询预警信息
     *
     * @param ruleCode 规则编号
     * @return 预警信息集合
     */
    List<AlertDTO> findByRuleCode(Long ruleCode);
}
