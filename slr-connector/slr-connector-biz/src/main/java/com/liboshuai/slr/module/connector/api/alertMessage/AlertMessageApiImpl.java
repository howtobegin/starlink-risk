package com.liboshuai.slr.module.connector.api.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespApiDTO;
import com.liboshuai.slr.module.connector.service.alertMessage.AlertMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AlertMessageApiImpl implements AlertMessageApi {

    @Resource
    private AlertMessageService alertMessageService;

    @Override
    public PageResult<AlertMessageRespApiDTO> page(AlertMessagePageReqApiDTO alertMessagePageReqApiDTO) {
        return alertMessageService.page(alertMessagePageReqApiDTO);
    }

    @Override
    public List<AlertMessageApiDTO> findByRuleCode(Long ruleCode) {
        return alertMessageService.findByRuleCode(ruleCode);
    }
}
