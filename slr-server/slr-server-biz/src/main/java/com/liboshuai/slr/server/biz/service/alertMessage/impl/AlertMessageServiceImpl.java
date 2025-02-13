package com.liboshuai.slr.server.biz.service.alertMessage.impl;

import com.liboshuai.slr.engine.api.dto.AlertMessageDTO;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.server.biz.controller.alert.vo.AlertPageReqVO;
import com.liboshuai.slr.server.biz.controller.alert.vo.AlertRespVO;
import com.liboshuai.slr.server.biz.convert.alert.AlertConvert;
import com.liboshuai.slr.server.biz.dal.dataobject.alert.MongoAlertDO;
import com.liboshuai.slr.server.biz.dal.mongo.alert.AlertMongoDAO;
import com.liboshuai.slr.server.biz.dal.mongo.alert.AlertRepository;
import com.liboshuai.slr.server.biz.service.alertMessage.AlertMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertMessageServiceImpl implements AlertMessageService {

    private final AlertMongoDAO alertMongoDAO;
    private final AlertRepository alertRepository;
    private final AlertConvert alertConvert;

    @Override
    public PageResult<AlertRespVO> page(AlertPageReqVO alertPageReqVO) {
        PageResult<MongoAlertDO> alertMessageDOPageResult = alertMongoDAO.selectPage(alertPageReqVO);
        return BeanUtils.toBean(alertMessageDOPageResult, AlertRespVO.class);
    }

    @Override
    public List<AlertMessageDTO> findByRuleCode(Long ruleCode) {
        List<MongoAlertDO> mongoAlertDOList = alertRepository.findByRuleCode(ruleCode);
        return alertConvert.batchConvertMongo2Dto(mongoAlertDOList);
    }
}
