package com.liboshuai.slr.module.connector.service.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespDTO;

import java.util.List;

public interface AlertMessageService {
    /**
     * 预警信息 分页查询
     */
    PageResult<AlertMessageRespDTO> page(AlertMessagePageReqDTO alertMessagePageReqDTO);

    /**
     * 根据规则编号查询预警信息
     *
     * @param ruleCode 规则编号
     * @return 预警信息集合
     */
    List<AlertMessageDTO> findByRuleCode(Long ruleCode);
}
