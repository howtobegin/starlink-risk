package com.liboshuai.slr.module.connector.api.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespDTO;

import java.util.List;

public interface AlertMessageApi {
    PageResult<AlertMessageRespDTO> page(AlertMessagePageReqDTO alertMessagePageReqDTO);

    List<AlertMessageDTO> findByRuleCode(Long ruleCode);
}
