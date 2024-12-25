package com.liboshuai.slr.module.connector.service.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageReqDTO;
import com.liboshuai.slr.module.engine.dto.AlertMessageDTO;

public interface AlertMessageService {

    /**
     * 预警信息 分页查询
     */
    PageResult<AlertMessageDTO> list(AlertMessageReqDTO alertMessageReqDTO);
}
