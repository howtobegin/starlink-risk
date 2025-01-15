package com.liboshuai.slr.module.connector.service.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespApiDTO;

import java.util.List;

public interface AlertMessageService {
    /**
     * 预警信息 分页查询
     */
    PageResult<AlertMessageRespApiDTO> page(AlertMessagePageReqApiDTO alertMessagePageReqApiDTO);

    /**
     * 根据规则编号查询预警信息
     *
     * @param ruleCode 规则编号
     * @return 预警信息集合
     */
    List<AlertMessageApiDTO> findByRuleCode(Long ruleCode);
}
