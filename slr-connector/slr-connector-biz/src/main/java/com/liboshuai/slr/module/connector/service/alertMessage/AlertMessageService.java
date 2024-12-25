package com.liboshuai.slr.module.connector.service.alertMessage;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.controller.alertMessage.vo.AlertMessageReqVO;
import com.liboshuai.slr.module.connector.controller.alertMessage.vo.AlertMessageRespVO;

public interface AlertMessageService {

    /**
     * 预警信息 分页查询
     */
    PageResult<AlertMessageRespVO> list(AlertMessageReqVO alertMessageReqVO);
}
