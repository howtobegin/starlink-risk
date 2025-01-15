package com.liboshuai.slr.module.connector.service.alertMessage.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespApiDTO;
import com.liboshuai.slr.module.connector.convert.alertMessage.AlertMessageConvert;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import com.liboshuai.slr.module.connector.dal.mongo.AlertMessageMongoDAO;
import com.liboshuai.slr.module.connector.dal.mongo.AlertMessageRepository;
import com.liboshuai.slr.module.connector.service.alertMessage.AlertMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertMessageServiceImpl implements AlertMessageService {

    private final AlertMessageMongoDAO alertMessageMongoDAO;
    private final AlertMessageRepository alertMessageRepository;
    private final AlertMessageConvert alertMessageConvert;

    @Override
    public PageResult<AlertMessageRespApiDTO> page(AlertMessagePageReqApiDTO alertMessagePageReqApiDTO) {
        PageResult<AlertMessageDO> alertMessageDOPageResult = alertMessageMongoDAO.selectPage(alertMessagePageReqApiDTO);
        return BeanUtils.toBean(alertMessageDOPageResult, AlertMessageRespApiDTO.class);
    }

    @Override
    public List<AlertMessageApiDTO> findByRuleCode(Long ruleCode) {
        List<AlertMessageDO> alertMessageDOList = alertMessageRepository.findByRuleCode(ruleCode);
        return alertMessageConvert.batchConvertMongo2Dto(alertMessageDOList);
    }
}
