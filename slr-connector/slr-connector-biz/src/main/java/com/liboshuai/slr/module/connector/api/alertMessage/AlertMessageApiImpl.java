package com.liboshuai.slr.module.connector.api.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageReqDTO;
import com.liboshuai.slr.module.connector.service.alertMessage.AlertMessageService;
import com.liboshuai.slr.module.engine.dto.AlertMessageDTO;
import io.swagger.v3.oas.annotations.servers.Server;

import javax.annotation.Resource;

@Server
public class AlertMessageApiImpl implements AlertMessageApi {

    @Resource
    private AlertMessageService alertMessageService;

    public PageResult<AlertMessageDTO> list(AlertMessageReqDTO alertMessageReqDTO) {
        return alertMessageService.list(alertMessageReqDTO);
    }
}
