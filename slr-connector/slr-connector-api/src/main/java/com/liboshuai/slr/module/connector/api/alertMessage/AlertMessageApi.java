package com.liboshuai.slr.module.connector.api.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespApiDTO;

import java.util.List;

public interface AlertMessageApi {
    PageResult<AlertMessageRespApiDTO> page(AlertMessagePageReqApiDTO alertMessagePageReqApiDTO);

    List<AlertMessageApiDTO> findByRuleCode(Long ruleCode);
}
