package com.liboshuai.slr.module.connector.service.alertMessage.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespDTO;
import com.liboshuai.slr.module.connector.convert.alertMessage.AlertMessageConvert;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import com.liboshuai.slr.module.connector.dal.mongo.AlertMessageMongoDAO;
import com.liboshuai.slr.module.connector.dal.mongo.AlertMessageRepository;
import com.liboshuai.slr.module.connector.service.alertMessage.AlertMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AlertMessageServiceImpl implements AlertMessageService {

    @Resource
    private AlertMessageMongoDAO alertMessageMongoDAO;
    @Resource
    private AlertMessageRepository alertMessageRepository;
    @Resource
    private AlertMessageConvert alertMessageConvert;

    @Override
    public PageResult<AlertMessageRespDTO> page(AlertMessagePageReqDTO alertMessagePageReqDTO) {
        PageResult<AlertMessageDO> alertMessageDOPageResult = alertMessageMongoDAO.selectPage(alertMessagePageReqDTO);
        return BeanUtils.toBean(alertMessageDOPageResult, AlertMessageRespDTO.class);
    }

    @Override
    public List<AlertMessageDTO> findByRuleCode(Long ruleCode) {
        List<AlertMessageDO> alertMessageDOList = alertMessageRepository.findByRuleCode(ruleCode);
        return alertMessageConvert.batchConvertMongo2Dto(alertMessageDOList);
    }
}
