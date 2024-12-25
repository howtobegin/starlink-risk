package com.liboshuai.slr.module.connector.service.alertMessage.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.connector.controller.alertMessage.vo.AlertMessageReqVO;
import com.liboshuai.slr.module.connector.controller.alertMessage.vo.AlertMessageRespVO;
import com.liboshuai.slr.module.connector.dal.mongo.AlertMessageMongoDAO;
import com.liboshuai.slr.module.connector.service.alertMessage.AlertMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AlertMessageServiceImpl implements AlertMessageService {

    @Resource
    private AlertMessageMongoDAO alertMessageMongoDAO;

    @Override
    public PageResult<AlertMessageRespVO> list(AlertMessageReqVO alertMessageReqVO) {
        return BeanUtils.toBean(alertMessageMongoDAO.selectPage(alertMessageReqVO), AlertMessageRespVO.class);
    }
}
