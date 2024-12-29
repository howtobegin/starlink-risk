package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.alibaba.fastjson.JSON;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.*;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.*;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.*;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.*;
import com.liboshuai.slr.module.admin.framework.component.snowflake.SnowflakeIdGenerator;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import com.liboshuai.slr.module.engine.dto.*;
import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    private RuleJsonMapper ruleJsonMapper;
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
            ruleEventAttrValueSaveReqVOList.forEach(
                    ruleEventAttrValueSaveReqVO ->
                            ruleEventAttrValueSaveReqVO.setCondCode(condCode)
            );
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RuleInfoSaveReqVO ruleInfoSaveReqVO) {
        String ruleCode = ruleInfoSaveReqVO.getRuleCode();
        // 参数效验
        validateParameter(ruleCode);
        // 更新 规则信息
        RuleInfoDO ruleInfoDO = BeanUtils.toBean(ruleInfoSaveReqVO, RuleInfoDO.class);
        ruleInfoMapper.updateByRuleCode(ruleInfoDO, ruleCode);
        // 更新 条件信息
        List<RuleCondSaveReqVO> ruleCondSaveReqVOList = ruleInfoSaveReqVO.getRuleCondSaveReqVOList();
        List<RuleCondDO> ruleCondDOList = BeanUtils.toBean(ruleCondSaveReqVOList, RuleCondDO.class);
        ruleCondMapper.deleteByRuleCode(ruleCode);
        ruleCondMapper.insertBatch(ruleCondDOList);
        // 更新 事件属性值信息
        List<String> condCodeList = ruleCondSaveReqVOList.stream().map(RuleCondSaveReqVO::getCondCode).collect(Collectors.toList());
        List<RuleEventAttrValueSaveReqVO> ruleEventAttrValueSaveReqVOList = ruleCondSaveReqVOList.stream()
                .flatMap(ruleCondSaveReqVO -> ruleCondSaveReqVO.getRuleEventAttrValueSaveReqVOList().stream())
                .collect(Collectors.toList());
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = BeanUtils.toBean(ruleEventAttrValueSaveReqVOList, RuleEventAttrValueDO.class);
        ruleEventAttrValueMapper.deleteByCondCodes(condCodeList);
        ruleEventAttrValueMapper.insertBatch(ruleEventAttrValueDOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(RuleInfoChangeStatusReqVO ruleInfoChangeStatusReqVO) {
        String ruleCode = ruleInfoChangeStatusReqVO.getRuleCode();
        String auditOp = ruleInfoChangeStatusReqVO.getAuditOp();
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        String ruleStatus = ruleInfoDO.getRuleStatus();
        String newRuleStatus = ruleInfoChangeStatusReqVO.getNewRuleStatus();
        if (Objects.equals(newRuleStatus, RuleStatusEnum.ONLINE_PENDING.getCode())) {
            // 进行上线操作
            if (!Objects.equals(ruleStatus, RuleStatusEnum.DRAFT.getCode()) && !Objects.equals(ruleStatus, RuleStatusEnum.OFFLINE.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_STATUS_NOT_DRAFT, ruleCode);
            }
            ruleInfoDO.setRuleStatus(newRuleStatus);
        } else if (Objects.equals(newRuleStatus, RuleStatusEnum.ONLINE.getCode())) {
            // 进行上线审核操作
            if (!Objects.equals(ruleStatus, RuleStatusEnum.ONLINE_PENDING.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_STATUS_NOT_ONLINE_PENDING, ruleCode);
            }
            if (Objects.equals(auditOp, RuleAuditOpEnum.APPROVE.getCode())) { // 审核通过
                ruleInfoDO.setRuleStatus(newRuleStatus);
            } else if (Objects.equals(auditOp, RuleAuditOpEnum.REJECT.getCode())) { // 审核拒绝
                ruleInfoDO.setRuleStatus(RuleStatusEnum.DRAFT.getCode());
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_AUDIT_OP_NOT_SUPPORT, ruleCode);
            }
            // 将规则数据存入 rule_json 表
            saveToRuleJson(ruleCode);
        } else if (Objects.equals(newRuleStatus, RuleStatusEnum.OFFLINE_PENDING.getCode())) {
            // 进行下线操作
            if (!Objects.equals(ruleStatus, RuleStatusEnum.ONLINE.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_STATUS_NOT_ONLINE, ruleCode);
            }
            ruleInfoDO.setRuleStatus(newRuleStatus);

        } else if (Objects.equals(newRuleStatus, RuleStatusEnum.OFFLINE.getCode())) {
            // 进行下线审核操作
            if (!Objects.equals(ruleStatus, RuleStatusEnum.OFFLINE_PENDING.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_STATUS_NOT_OFFLINE_PENDING, ruleCode);
            }
            if (Objects.equals(auditOp, RuleAuditOpEnum.APPROVE.getCode())) { // 审核通过
                ruleInfoDO.setRuleStatus(newRuleStatus);
            } else if (Objects.equals(auditOp, RuleAuditOpEnum.REJECT.getCode())) { // 审核拒绝
                ruleInfoDO.setRuleStatus(RuleStatusEnum.ONLINE.getCode());
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_AUDIT_OP_NOT_SUPPORT, ruleCode);
            }
            // 将规则数据从 rule_json 表删除
            ruleJsonMapper.delete(RuleJsonDO::getRuleCode, ruleCode);
        } else {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NEW_STATUS_NOT_SUPPORT, ruleCode);
        }
        ruleInfoMapper.updateByRuleCode(ruleInfoDO, ruleCode);
    }

    /**
     * 将规则数据存入 rule_json 表
     */
    private void saveToRuleJson(String ruleCode) {
        Long count = ruleJsonMapper.selectCount(RuleJsonDO::getRuleCode, ruleCode);
        if (Objects.isNull(count)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_JSON_COUNT_SELECT_ERROR, ruleCode);
        }
        if (count > 0) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_JSON_EXISTS, ruleCode);
        }
        RuleInfoDTO ruleInfoDTO = getRuleInfoDtoByRuleCode(ruleCode);
        RuleJsonDO ruleJsonDO = RuleJsonDO.builder().ruleCode(ruleCode).ruleJson(JSON.toJSONString(ruleInfoDTO)).build();
        ruleJsonMapper.insert(ruleJsonDO);
    }

    /**
     * 根据规则编号获取规则信息DTO
     */
    private RuleInfoDTO getRuleInfoDtoByRuleCode(String ruleCode) {
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        RuleInfoDTO ruleInfoDTO = BeanUtils.toBean(ruleInfoDO, RuleInfoDTO.class);
        List<RuleCondDO> ruleCondDOList = ruleCondMapper.selectListByRuleCode(ruleCode);
        if (CollectionUtils.isEmpty(ruleCondDOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_COND_NOT_EXISTS, ruleCode);
        }
        List<RuleCondDTO> ruleCondDTOList = BeanUtils.toBean(ruleCondDOList, RuleCondDTO.class);
        List<String> ruleCondCodeList = ruleCondDTOList.stream().map(RuleCondDTO::getCondCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleCondCodeList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_CONDITION_CODE_IS_NULL);
        }
        List<String> ruleEventCodeList = ruleCondDTOList.stream().map(RuleCondDTO::getEventCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleEventCodeList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_CONDITION_EVENT_CODE_IS_NULL);
        }
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_NOT_EXISTS, ruleEventCodeList);
        }
        List<RuleEventDTO> ruleEventDTOList = BeanUtils.toBean(ruleEventDOList, RuleEventDTO.class);
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_ATTR_NOT_EXISTS, ruleEventCodeList);
        }
        List<RuleEventAttrDTO> ruleEventAttrDTOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrDTO.class);
        // 给 属性 设置 属性值
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = ruleEventAttrValueMapper.selectListByCondCodes(ruleCondCodeList);
        if (!CollectionUtils.isEmpty(ruleEventAttrValueDOList)) {
            List<RuleEventAttrValueDTO> ruleEventAttrValueDTOList = BeanUtils.toBean(ruleEventAttrValueDOList, RuleEventAttrValueDTO.class);
            Map<String, List<RuleEventAttrValueDTO>> attrCodeAndAttrValueDtoMap = ruleEventAttrValueDTOList.stream()
                    .collect(Collectors.groupingBy(RuleEventAttrValueDTO::getAttributeCode));
            ruleEventAttrDTOList.forEach(
                    ruleEventAttrDTO -> {
                        List<RuleEventAttrValueDTO> ruleEventAttrValueDTOS = attrCodeAndAttrValueDtoMap.get(ruleEventAttrDTO.getAttributeCode());
                        if (!CollectionUtils.isEmpty(ruleEventAttrValueDTOS)) {
                            ruleEventAttrDTO.setRuleEventAttrValueDTO(ruleEventAttrValueDTOS.get(0));
                        }
                    }
            );
        }
        // 给 事件 设置 属性
        Map<String, List<RuleEventAttrDTO>> eventCodeAndkEventAttrDtoMap = ruleEventAttrDTOList.stream()
                .collect(Collectors.groupingBy(RuleEventAttrDTO::getEventCode));
        ruleEventDTOList.forEach(
                ruleEventDTO ->
                        ruleEventDTO.setRuleEventAttrDTOList(eventCodeAndkEventAttrDtoMap.get(ruleEventDTO.getEventCode()))
        );
        // 给 条件 设置 事件
        Map<String, List<RuleEventDTO>> eventCodeAndEventDtoMap = ruleEventDTOList.stream()
                .collect(Collectors.groupingBy(RuleEventDTO::getEventCode));
        ruleCondDTOList.forEach(
                ruleCondDTO -> {
                    List<RuleEventDTO> ruleEventDTOS = eventCodeAndEventDtoMap.get(ruleCondDTO.getEventCode());
                    if (!CollectionUtils.isEmpty(ruleEventDTOS)) {
                        ruleCondDTO.setRuleEventDTO(ruleEventDTOS.get(0));
                    }
                }
        );
        // 给 规则 设置 条件
        ruleInfoDTO.setRuleCondDTOList(ruleCondDTOList);
        return ruleInfoDTO;
    }

    /**
     * 参数效验
     */
    private void validateParameter(String ruleCode) {
        if (!StringUtils.hasText(ruleCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_CODE_NOT_BLANK);
        }
        if (Objects.isNull(ruleInfoMapper.selectOneByRuleCode(ruleCode))) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
    }

    /**
     * 设置目标信息
     */
    private void detailSetRuleKey(RuleInfoRespVO ruleInfoRespVO) {
        String keyCode = ruleInfoRespVO.getKeyCode();
        if (!StringUtils.hasText(keyCode)) {
            return;
        }
        RuleTargetDO ruleTargetDO = ruleKeyMapper.selectOneByKeyCode(keyCode);
        if (Objects.isNull(ruleTargetDO)) {
            return;
        }
        RuleKeyRespVO ruleKeyRespVO = BeanUtils.toBean(ruleTargetDO, RuleKeyRespVO.class);
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
        List<RuleCondDO> ruleCondDOList = ruleCondMapper.selectListByRuleCode(ruleCode);
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
            ruleCondRespVOList.forEach(ruleCondRespVO -> {
                List<RuleEventRespVO> ruleEventRespVOS = envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode());
                if (!CollectionUtils.isEmpty(ruleEventRespVOS)) {
                    ruleCondRespVO.setRuleEventRespVO(ruleEventRespVOS.get(0));
                }
            });
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrValueRespVO> ruleEventAttrValueRespVOList = BeanUtils.toBean(ruleEventAttrValueDOList, RuleEventAttrValueRespVO.class);
        Map<String, List<RuleEventAttrValueRespVO>> codeAndAttrValueRespVoMap = ruleEventAttrValueRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventAttrValueRespVO::getAttributeCode));
        List<String> attributeCodeList = ruleEventAttrValueRespVOList.stream().map(RuleEventAttrValueRespVO::getAttributeCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attributeCodeList)) {
            ruleCondRespVOList.forEach(ruleCondRespVO -> {
                List<RuleEventRespVO> ruleEventRespVOS = envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode());
                if (!CollectionUtils.isEmpty(ruleEventRespVOS)) {
                    ruleCondRespVO.setRuleEventRespVO(ruleEventRespVOS.get(0));
                }
            });
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByAttributeCodes(attributeCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            ruleCondRespVOList.forEach(ruleCondRespVO -> {
                List<RuleEventRespVO> ruleEventRespVOS = envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode());
                if (!CollectionUtils.isEmpty(ruleEventRespVOS)) {
                    ruleCondRespVO.setRuleEventRespVO(ruleEventRespVOS.get(0));
                }
            });
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        // 给 属性 设置 属性值
        List<RuleEventAttrRespVO> ruleEventAttrRespVOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
        ruleEventAttrRespVOList.forEach(ruleEventAttrRespVO -> {
            List<RuleEventAttrValueRespVO> ruleEventAttrValueRespVOS = codeAndAttrValueRespVoMap.get(ruleEventAttrRespVO.getAttributeCode());
            if (!CollectionUtils.isEmpty(ruleEventAttrValueRespVOS)) {
                ruleEventAttrRespVO.setRuleEventAttrValueRespVO(ruleEventAttrValueRespVOS.get(0));
            }
        });
        // 给 事件 设置 属性
        Map<String, List<RuleEventAttrRespVO>> eventCodeAndEventAttrRespVoMap = ruleEventAttrRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventAttrRespVO::getEventCode));
        ruleEventRespVOList.forEach(ruleEventRespVO -> ruleEventRespVO.setRuleEventAttrRespVoList(eventCodeAndEventAttrRespVoMap.get(ruleEventRespVO.getEventCode())));
        // 给 条件 设置 事件
        Map<String, List<RuleEventRespVO>> eventCodeAndEventRespVoMap = ruleEventRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventRespVO::getEventCode));
        ruleCondRespVOList.forEach(ruleCondRespVO -> {
            List<RuleEventRespVO> ruleEventRespVOS = eventCodeAndEventRespVoMap.get(ruleCondRespVO.getEventCode());
            if (!CollectionUtils.isEmpty(ruleEventRespVOS)) {
                ruleCondRespVO.setRuleEventRespVO(ruleEventRespVOS.get(0));
            }
        });
        // 给 规则 设置 条件
        ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
    }
}
