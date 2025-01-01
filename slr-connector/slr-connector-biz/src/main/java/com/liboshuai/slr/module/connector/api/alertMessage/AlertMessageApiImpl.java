package com.liboshuai.slr.module.connector.api.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespDTO;
import com.liboshuai.slr.module.connector.service.alertMessage.AlertMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AlertMessageApiImpl implements AlertMessageApi {

    @Resource
    private AlertMessageService alertMessageService;

    @Override
    public PageResult<AlertMessageRespDTO> page(AlertMessagePageReqDTO alertMessagePageReqDTO) {
        return alertMessageService.page(alertMessagePageReqDTO);
    }
}
