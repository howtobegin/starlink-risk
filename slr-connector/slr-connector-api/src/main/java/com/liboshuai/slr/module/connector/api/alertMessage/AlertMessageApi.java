package com.liboshuai.slr.module.connector.api.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespDTO;

public interface AlertMessageApi {
    PageResult<AlertMessageRespDTO> page(AlertMessagePageReqDTO alertMessagePageReqDTO);
}
