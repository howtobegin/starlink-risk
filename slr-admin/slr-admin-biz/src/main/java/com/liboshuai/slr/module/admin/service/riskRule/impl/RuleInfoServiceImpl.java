package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleCondSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventAttrValueSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.*;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.*;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.*;
import com.liboshuai.slr.module.admin.framework.component.snowflake.SnowflakeIdGenerator;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.liboshuai.slr.module.engine.constants.SnowflakeIdPrefixConstants.COND_CODE_PREFIX;
import static com.liboshuai.slr.module.engine.constants.SnowflakeIdPrefixConstants.RULE_CODE_PREFIX;

@Service
public class RuleInfoServiceImpl implements RuleInfoService {
    @Resource
    private RuleInfoMapper ruleInfoMapper;
    @Resource
    private RuleModelMapper ruleModelMapper;
    @Resource
    private RuleCondMapper ruleCondMapper;
    @Resource
    private RuleEventMapper ruleEventMapper;
    @Resource
    private RuleEventAttrMapper ruleEventAttrMapper;
    @Resource
    private RuleEventAttrValueMapper ruleEventAttrValueMapper;
    @Resource
    private RuleKeyMapper ruleKeyMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public PageResult<RuleInfoRespVO> list(RuleInfoPageReqVO ruleInfoPageReqVO) {
        PageResult<RuleInfoDO> ruleInfoEntityPageResult = ruleInfoMapper.selectPage(ruleInfoPageReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleInfoRespVO.class);
    }

    @Override
    public RuleInfoRespVO detail(String ruleCode) {
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            return new RuleInfoRespVO();
        }
        RuleInfoRespVO ruleInfoRespVO = BeanUtils.toBean(ruleInfoDO, RuleInfoRespVO.class);
        // 设置目标信息
        detailSetRuleKey(ruleInfoRespVO);
        // 设置模型信息
        detailSetRuleModel(ruleInfoRespVO);
        // 设置条件组
        detailSetRuleCondGroup(ruleCode, ruleInfoRespVO);
        return ruleInfoRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(RuleInfoSaveReqVO ruleInfoSaveReqVO) {

        // 处理 规则信息
        String ruleCode = RULE_CODE_PREFIX + snowflakeIdGenerator.nextIdStr(); // 生成 规则编号
        ruleInfoSaveReqVO.setRuleCode(ruleCode);
        ruleInfoSaveReqVO.setRuleStatus(RuleStatusEnum.DRAFT.getCode());  // 设置 初始规则状态
        RuleInfoDO ruleInfoDO = BeanUtils.toBean(ruleInfoSaveReqVO, RuleInfoDO.class); // 对象转换
        ruleInfoMapper.insert(ruleInfoDO); // 保存 规则信息

        // 处理 条件信息 与 事件属性值信息
        List<RuleCondSaveReqVO> ruleCondSaveReqVOList = ruleInfoSaveReqVO.getRuleCondSaveReqVOList();
        List<RuleCondSaveReqVO> newRuleCondSaveReqVOList = new ArrayList<>();
        List<RuleEventAttrValueSaveReqVO> newRuleEventAttrValueSaveReqVOList = new ArrayList<>();
        for (RuleCondSaveReqVO ruleCondSaveReqVO : ruleCondSaveReqVOList) {
            // 条件信息设置 规则编号
            ruleCondSaveReqVO.setRuleCode(ruleCode);
            // 条件信息生成 条件编号
            String condCode = COND_CODE_PREFIX + snowflakeIdGenerator.nextIdStr();
            ruleCondSaveReqVO.setCondCode(condCode);
            newRuleCondSaveReqVOList.add(ruleCondSaveReqVO);

            // 事件属性值信息设置 条件编号
            List<RuleEventAttrValueSaveReqVO> ruleEventAttrValueSaveReqVOList =
                    ruleCondSaveReqVO.getRuleEventAttrValueSaveReqVOList();
            ruleEventAttrValueSaveReqVOList = ruleEventAttrValueSaveReqVOList.stream()
                    .peek(ruleEventAttrValueSaveReqVO ->
                            ruleEventAttrValueSaveReqVO.setCondCode(condCode))
                    .collect(Collectors.toList());
            newRuleEventAttrValueSaveReqVOList.addAll(ruleEventAttrValueSaveReqVOList);
        }
        // 保存 条件信息
        List<RuleCondDO> ruleCondDOList = BeanUtils.toBean(newRuleCondSaveReqVOList, RuleCondDO.class);
        ruleCondMapper.insertBatch(ruleCondDOList);

        // 保存 事件属性值信息
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = BeanUtils.toBean(newRuleEventAttrValueSaveReqVOList,
                RuleEventAttrValueDO.class);
        ruleEventAttrValueMapper.insertBatch(ruleEventAttrValueDOList);
        return ruleCode;
    }

    /**
     * 设置目标信息
     */
    private void detailSetRuleKey(RuleInfoRespVO ruleInfoRespVO) {
        String keyCode = ruleInfoRespVO.getKeyCode();
        if (!StringUtils.hasText(keyCode)) {
            return;
        }
        RuleKeyDO ruleKeyDO = ruleKeyMapper.selectOneByKeyCode(keyCode);
        if (Objects.isNull(ruleKeyDO)) {
            return;
        }
        RuleKeyRespVO ruleKeyRespVO = BeanUtils.toBean(ruleKeyDO, RuleKeyRespVO.class);
        ruleInfoRespVO.setRuleKeyRespVO(ruleKeyRespVO);
    }

    /**
     * 设置模型信息
     */
    private void detailSetRuleModel(RuleInfoRespVO ruleInfoRespVO) {
        String modelCode = ruleInfoRespVO.getModelCode();
        if (StringUtils.hasText(modelCode)) {
            RuleModelDO ruleModelDO = ruleModelMapper.selectOneByModelCode(modelCode);
            RuleModelRespVO ruleModelRespVO = BeanUtils.toBean(ruleModelDO, RuleModelRespVO.class);
            ruleInfoRespVO.setRuleModelRespVO(ruleModelRespVO);
        }
    }

    /**
     * 设置条件组
     */
    private void detailSetRuleCondGroup(String ruleCode, RuleInfoRespVO ruleInfoRespVO) {
        List<RuleCondDO> ruleCondDOList = ruleCondMapper.selectListByModelCode(ruleCode);
        if (CollectionUtils.isEmpty(ruleCondDOList)) {
            return;
        }
        List<RuleCondRespVO> ruleCondRespVOList = BeanUtils.toBean(ruleCondDOList, RuleCondRespVO.class);
        List<String> eventCodeList = ruleCondRespVOList.stream().map(RuleCondRespVO::getEventCode).collect(Collectors.toList());
        List<String> condCodeList = ruleCondRespVOList.stream().map(RuleCondRespVO::getCondCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(eventCodeList) || CollectionUtils.isEmpty(condCodeList)) {
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByEventCodes(eventCodeList);
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventRespVO> ruleEventRespVOList = BeanUtils.toBean(ruleEventDOList, RuleEventRespVO.class);
        Map<String, List<RuleEventRespVO>> envetCodeAndRuleEventRespVoMap = ruleEventRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventRespVO::getEventCode));
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = ruleEventAttrValueMapper.selectListByCondCodes(condCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrValueDOList)) {
            ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrValueRespVO> ruleEventAttrValueRespVOList = BeanUtils.toBean(ruleEventAttrValueDOList, RuleEventAttrValueRespVO.class);
        Map<String, List<RuleEventAttrValueRespVO>> codeAndAttrValueRespVoMap = ruleEventAttrValueRespVOList.stream().collect(Collectors.groupingBy(RuleEventAttrValueRespVO::getAttributeCode));
        List<String> attributeCodeList = ruleEventAttrValueRespVOList.stream().map(RuleEventAttrValueRespVO::getAttributeCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attributeCodeList)) {
            ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByAttributeCodes(attributeCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrRespVO> ruleEventAttrRespVOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
        ruleEventAttrRespVOList = ruleEventAttrRespVOList.stream().map(ruleEventAttrRespVO -> ruleEventAttrRespVO.setRuleEventAttrValueRespVO(codeAndAttrValueRespVoMap.get(ruleEventAttrRespVO.getAttributeCode()).get(0))).collect(Collectors.toList());
        Map<String, List<RuleEventAttrRespVO>> eventCodeAndEventAttrRespVoMap = ruleEventAttrRespVOList.stream().collect(Collectors.groupingBy(RuleEventAttrRespVO::getEventCode));
        ruleEventRespVOList = ruleEventRespVOList.stream().map(ruleEventRespVO -> ruleEventRespVO.setRuleEventAttrRespVoList(eventCodeAndEventAttrRespVoMap.get(ruleEventRespVO.getEventCode()))).collect(Collectors.toList());
        Map<String, List<RuleEventRespVO>> eventCodeAndEventRespVoMap = ruleEventRespVOList.stream().collect(Collectors.groupingBy(RuleEventRespVO::getEventCode));
        ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(eventCodeAndEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
        ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
    }
}
