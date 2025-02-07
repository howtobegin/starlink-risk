package com.liboshuai.slr.server.biz.service.alertMessage.impl;

import com.liboshuai.slr.engine.api.dto.AlertMessageDTO;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessagePageReqVO;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessageRespVO;
import com.liboshuai.slr.server.biz.convert.alertMessage.AlertMessageConvert;
import com.liboshuai.slr.server.biz.dal.dataobject.alertMessage.AlertMessageDO;
import com.liboshuai.slr.server.biz.dal.mongo.AlertMessageMongoDAO;
import com.liboshuai.slr.server.biz.dal.mongo.AlertMessageRepository;
import com.liboshuai.slr.server.biz.service.alertMessage.AlertMessageService;
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
    public PageResult<AlertMessageRespVO> page(AlertMessagePageReqVO alertMessagePageReqVO) {
        PageResult<AlertMessageDO> alertMessageDOPageResult = alertMessageMongoDAO.selectPage(alertMessagePageReqVO);
        return BeanUtils.toBean(alertMessageDOPageResult, AlertMessageRespVO.class);
    }

    @Override
    public List<AlertMessageDTO> findByRuleCode(Long ruleCode) {
        List<AlertMessageDO> alertMessageDOList = alertMessageRepository.findByRuleCode(ruleCode);
        return alertMessageConvert.batchConvertMongo2Dto(alertMessageDOList);
    }
}
