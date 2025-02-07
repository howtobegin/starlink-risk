package com.liboshuai.slr.server.biz.service.riskRule.impl;

import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleEventAttrRespVO;
import com.liboshuai.slr.server.biz.dal.dataobject.riskRule.RuleEventAttrDO;
import com.liboshuai.slr.server.biz.dal.mysql.riskRule.RuleEventAttrMapper;
import com.liboshuai.slr.server.biz.service.riskRule.RuleEventAttrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleEventAttrServiceImpl implements RuleEventAttrService {

    private final RuleEventAttrMapper ruleEventAttrMapper;

    @Override
    public List<RuleEventAttrRespVO> list(String eventCode) {
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCode(eventCode);
        return BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
    }

    @Override
    public Boolean checkUniqueEventAttrCode(String eventAttrCode) {
        return ruleEventAttrMapper.selectListByEventCode(eventAttrCode) == null;
    }
}
