package com.liboshuai.slr.server.biz.service.riskRule.impl;

import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdGenerator;
import com.liboshuai.slr.server.api.constants.ErrorCodeConstants;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleModelCreateReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleModelPageReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleModelUpdateReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleModelRespVO;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleModelDO;
import com.liboshuai.slr.server.biz.dal.mysql.rule.RuleModelMapper;
import com.liboshuai.slr.server.biz.service.riskRule.RuleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleModelServiceImpl implements RuleModelService {

    private final RuleModelMapper ruleModelMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public PageResult<RuleModelRespVO> page(RuleModelPageReqVO ruleModelPageReqVO) {
        PageResult<RuleModelDO> ruleModelEntityPageResult = ruleModelMapper.selectPage(ruleModelPageReqVO);
        return BeanUtils.toBean(ruleModelEntityPageResult, RuleModelRespVO.class);
    }

    @Override
    public RuleModelRespVO detail(Long modelCode) {
        RuleModelDO ruleModelDO = ruleModelMapper.selectOneByModelCode(modelCode);
        return BeanUtils.toBean(ruleModelDO, RuleModelRespVO.class);
    }

    @Override
    public Long create(RuleModelCreateReqVO ruleModelCreateReqVO) {
        RuleModelDO ruleModelDO = BeanUtils.toBean(ruleModelCreateReqVO, RuleModelDO.class);
        long modelCode = snowflakeIdGenerator.nextId();
        ruleModelDO.setModelCode(modelCode);
        ruleModelMapper.insert(ruleModelDO);
        return modelCode;
    }

    @Override
    public void update(RuleModelUpdateReqVO ruleModelUpdateReqVO) {
        Long modelCode = ruleModelUpdateReqVO.getModelCode();
        Long count = ruleModelMapper.selectCount(RuleModelDO::getModelCode, modelCode);
        if (count == 0) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_MODEL_NOT_EXISTS, modelCode);
        }
        RuleModelDO ruleModelDO = BeanUtils.toBean(ruleModelUpdateReqVO, RuleModelDO.class);
        ruleModelMapper.updateByModelCode(ruleModelDO, modelCode);
    }
}
