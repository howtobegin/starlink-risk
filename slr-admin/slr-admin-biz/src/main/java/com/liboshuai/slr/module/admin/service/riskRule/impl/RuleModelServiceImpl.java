package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelCreateReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelUpdateReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleModelRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleModelDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleModelMapper;
import com.liboshuai.slr.module.admin.framework.component.snowflake.SnowflakeIdGenerator;
import com.liboshuai.slr.module.admin.service.riskRule.RuleModelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.liboshuai.slr.module.engine.constants.SnowflakeIdPrefixConstants.MODEL_CODE_PREFIX;

@Service
public class RuleModelServiceImpl implements RuleModelService {

    @Resource
    private RuleModelMapper ruleModelMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public PageResult<RuleModelRespVO> list(RuleModelPageReqVO ruleModelPageReqVO) {
        PageResult<RuleModelDO> ruleModelEntityPageResult = ruleModelMapper.selectPage(ruleModelPageReqVO);
        return BeanUtils.toBean(ruleModelEntityPageResult, RuleModelRespVO.class);
    }

    @Override
    public RuleModelRespVO detail(String modelCode) {
        RuleModelDO ruleModelDO = ruleModelMapper.selectOneByModelCode(modelCode);
        return BeanUtils.toBean(ruleModelDO, RuleModelRespVO.class);
    }

    @Override
    public String create(RuleModelCreateReqVO ruleModelCreateReqVO) {
        RuleModelDO ruleModelDO = BeanUtils.toBean(ruleModelCreateReqVO, RuleModelDO.class);
        String modelCode = MODEL_CODE_PREFIX + snowflakeIdGenerator.nextIdStr();
        ruleModelDO.setModelCode(modelCode);
        ruleModelMapper.insert(ruleModelDO);
        return modelCode;
    }

    @Override
    public void update(RuleModelUpdateReqVO ruleModelUpdateReqVO) {
        String modelCode = ruleModelUpdateReqVO.getModelCode();
        Long count = ruleModelMapper.selectCount(RuleModelDO::getModelCode, modelCode);
        if (count == 0) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_MODEL_NOT_EXISTS, modelCode);
        }
        RuleModelDO ruleModelDO = BeanUtils.toBean(ruleModelUpdateReqVO, RuleModelDO.class);
        ruleModelMapper.updateByModelCode(ruleModelDO, modelCode);
    }
}
