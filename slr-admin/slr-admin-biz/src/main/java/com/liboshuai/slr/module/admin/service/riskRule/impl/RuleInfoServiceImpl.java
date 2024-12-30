package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.alibaba.fastjson.JSON;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.*;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleCondRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventAttrValueRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.*;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.*;
import com.liboshuai.slr.module.admin.framework.component.snowflake.SnowflakeIdGenerator;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import com.liboshuai.slr.module.engine.dto.RuleCondDTO;
import com.liboshuai.slr.module.engine.dto.RuleEventAttrValueDTO;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private RuleTargetMapper ruleTargetMapper;
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
        detailSetRuleTarget(ruleInfoRespVO);
        // 设置模型信息
        detailSetRuleModel(ruleInfoRespVO);
        // 设置条件组
        detailSetRuleCondGroup(ruleInfoRespVO);
        return ruleInfoRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(RuleInfoSaveReqVO ruleInfoSaveReqVO) {

        // 保存 规则信息
        String ruleCode = RULE_CODE_PREFIX + snowflakeIdGenerator.nextIdStr(); // 生成 规则编号
        ruleInfoSaveReqVO.setRuleCode(ruleCode);
        ruleInfoSaveReqVO.setRuleStatus(RuleStatusEnum.DRAFT.getCode());  // 设置 初始规则状态
        RuleInfoDO ruleInfoDO = BeanUtils.toBean(ruleInfoSaveReqVO, RuleInfoDO.class); // 对象转换
        ruleInfoMapper.insert(ruleInfoDO);

        // 保存 条件信息
        List<RuleCondSaveReqVO> ruleCondSaveReqVOList = ruleInfoSaveReqVO.getRuleCondGroup();
        ruleCondSaveReqVOList.forEach(ruleCondSaveReqVO -> {
            ruleCondSaveReqVO.setRuleCode(ruleCode);
            ruleCondSaveReqVO.setCondCode(ruleCode + DefaultConstants.UNDERSCORE + ruleCondSaveReqVO.getEventCode());
            List<RuleEventAttrValueSaveReqVO> ruleEventAttrValueGroup = ruleCondSaveReqVO.getRuleEventAttrValueGroup();
            if (!CollectionUtils.isEmpty(ruleEventAttrValueGroup)) {
                ruleEventAttrValueGroup.forEach(ruleEventAttrValueSaveReqVO -> {
                    ruleEventAttrValueSaveReqVO.setCondCode(ruleCondSaveReqVO.getCondCode());
                });
            }
        }); // 条件信息设置 规则编号、条件编号
        List<RuleCondDO> ruleCondDOList = BeanUtils.toBean(ruleCondSaveReqVOList, RuleCondDO.class);
        ruleCondMapper.insertBatch(ruleCondDOList);

        // 保存 事件属性值信息
        List<RuleEventAttrValueSaveReqVO> ruleEventAttrValueGroup = ruleCondSaveReqVOList.stream()
                .filter(ruleCondSaveReqVO -> !CollectionUtils.isEmpty(ruleCondSaveReqVO.getRuleEventAttrValueGroup()))
                .flatMap(ruleCondSaveReqVO -> ruleCondSaveReqVO.getRuleEventAttrValueGroup().stream())
                .collect(Collectors.toList());
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = BeanUtils.toBean(ruleEventAttrValueGroup, RuleEventAttrValueDO.class);
        ruleEventAttrValueMapper.insertBatch(ruleEventAttrValueDOList);
        return ruleCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RuleInfoSaveReqVO ruleInfoSaveReqVO) {
        String ruleCode = ruleInfoSaveReqVO.getRuleCode();
        // 效验
        validate(ruleCode);
        // 更新 规则信息
        RuleInfoDO ruleInfoDO = BeanUtils.toBean(ruleInfoSaveReqVO, RuleInfoDO.class);
        ruleInfoMapper.updateByRuleCode(ruleInfoDO, ruleCode);
        // 更新 条件信息
        List<RuleCondSaveReqVO> ruleCondSaveReqVOList = ruleInfoSaveReqVO.getRuleCondGroup();
        List<RuleCondDO> ruleCondDOList = BeanUtils.toBean(ruleCondSaveReqVOList, RuleCondDO.class);
        ruleCondMapper.deleteByRuleCode(ruleCode);
        ruleCondMapper.insertBatch(ruleCondDOList);
        // 更新 事件属性值信息
        List<String> condCodeList = ruleCondSaveReqVOList.stream().map(RuleCondSaveReqVO::getCondCode).collect(Collectors.toList());
        List<RuleEventAttrValueSaveReqVO> ruleEventAttrValueSaveReqVOList = ruleCondSaveReqVOList.stream()
                .filter(ruleCondSaveReqVO -> !CollectionUtils.isEmpty(ruleCondSaveReqVO.getRuleEventAttrValueGroup()))
                .flatMap(ruleCondSaveReqVO -> ruleCondSaveReqVO.getRuleEventAttrValueGroup().stream())
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
        // 构建规则信息DTO
        RuleInfoDTO ruleInfoDTO = buildRuleInfoDTO(ruleCode);
        RuleJsonDO ruleJsonDO = RuleJsonDO.builder().ruleCode(ruleCode).ruleJson(JSON.toJSONString(ruleInfoDTO)).build();
        ruleJsonMapper.insert(ruleJsonDO);
    }

    /**
     * 构建规则信息DTO
     */
    private RuleInfoDTO buildRuleInfoDTO(String ruleCode) {
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        RuleInfoDTO ruleInfoDTO = BeanUtils.toBean(ruleInfoDO, RuleInfoDTO.class);
        // 设置 规则目标信息
        setRuleTarget(ruleInfoDO, ruleInfoDTO);
        // 设置 规则模型信息
        setRuleModel(ruleCode, ruleInfoDO, ruleInfoDTO);
        // 设置 规则条件信息
        RuleInfoDTO ruleInfoDTO1 = setRuleCodeGroup(ruleCode, ruleInfoDTO);
        if (ruleInfoDTO1 != null) return ruleInfoDTO1;
        return ruleInfoDTO;
    }

    private RuleInfoDTO setRuleCodeGroup(String ruleCode, RuleInfoDTO ruleInfoDTO) {
        List<RuleCondDO> ruleCondDOList = ruleCondMapper.selectListByRuleCode(ruleCode);
        if (CollectionUtils.isEmpty(ruleCondDOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_COND_NOT_EXISTS, ruleCode);
        }
        List<RuleCondDTO> ruleCondGroup = BeanUtils.toBean(ruleCondDOList, RuleCondDTO.class);
        List<String> ruleEventCodeList = ruleCondGroup.stream().map(RuleCondDTO::getEventCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleEventCodeList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_CONDITION_EVENT_CODE_IS_NULL);
        }
        // 给 条件组 设置 事件信息
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_NOT_EXISTS, ruleEventCodeList);
        }
        Map<String, RuleEventDO> eventCodeAndEventDoMap = ruleEventDOList.stream()
                .collect(Collectors.toMap(
                        RuleEventDO::getEventCode,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        ruleCondGroup.forEach(ruleCondDTO -> {
            RuleEventDO ruleEventDO = eventCodeAndEventDoMap.get(ruleCondDTO.getEventCode());
            ruleCondDTO.setEventCode(ruleEventDO.getEventCode());
            ruleCondDTO.setEventField(ruleEventDO.getEventField());
            ruleCondDTO.setEventName(ruleEventDO.getEventName());
        });
        // 给 条件组 设置 事件属性值
        List<String> condCodeList = ruleCondGroup.stream().map(RuleCondDTO::getCondCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(condCodeList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_CONDITION_COND_CODE_IS_NULL);
        }
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = ruleEventAttrValueMapper.selectListByCondCodes(condCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrValueDOList)) {
            ruleInfoDTO.setRuleCondGroup(ruleCondGroup);
            return ruleInfoDTO;
        }
        List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = BeanUtils.toBean(ruleEventAttrValueDOList, RuleEventAttrValueDTO.class);
        // 给 条件组 设置 事件属性
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            Map<String, List<RuleEventAttrValueDTO>> condCodeAndAttrValueMap = ruleEventAttrValueGroup.stream()
                    .collect(Collectors.groupingBy(RuleEventAttrValueDTO::getCondCode));
            ruleCondGroup.forEach(
                    ruleCondDTO ->
                            ruleCondDTO.setRuleEventAttrValueGroup(condCodeAndAttrValueMap.get(ruleCondDTO.getCondCode()))
            );
            ruleInfoDTO.setRuleCondGroup(ruleCondGroup);
            return ruleInfoDTO;
        }
        Map<String, RuleEventAttrDO> attrCodeAndAttrDoMap = ruleEventAttrDOList.stream()
                .collect(Collectors.toMap(
                        RuleEventAttrDO::getAttrCode, Function.identity(),
                        (existing, replacement) -> existing
                ));
        ruleEventAttrValueGroup.forEach(ruleEventAttrValueDTO -> {
            RuleEventAttrDO ruleEventAttrDO = attrCodeAndAttrDoMap.get(ruleEventAttrValueDTO.getAttrCode());
            ruleEventAttrValueDTO.setAttrField(ruleEventAttrDO.getAttrField());
            ruleEventAttrValueDTO.setAttrName(ruleEventAttrDO.getAttrName());
            ruleEventAttrValueDTO.setAttrType(ruleEventAttrDO.getAttrType());
        });
        Map<String, List<RuleEventAttrValueDTO>> condCodeAndAttrValueMap = ruleEventAttrValueGroup.stream()
                .collect(Collectors.groupingBy(RuleEventAttrValueDTO::getCondCode));
        ruleCondGroup.forEach(
                ruleCondDTO ->
                        ruleCondDTO.setRuleEventAttrValueGroup(condCodeAndAttrValueMap.get(ruleCondDTO.getCondCode()))
        );
        // 给 规则 设置 条件组
        ruleInfoDTO.setRuleCondGroup(ruleCondGroup);
        return null;
    }

    private void setRuleModel(String ruleCode, RuleInfoDO ruleInfoDO, RuleInfoDTO ruleInfoDTO) {
        String modelCode = ruleInfoDO.getModelCode();
        if (!StringUtils.hasText(modelCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_MODEL_CODE_IS_NULL, ruleCode);
        }
        RuleModelDO ruleModelDO = ruleModelMapper.selectOneByModelCode(modelCode);
        if (Objects.isNull(ruleModelDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_MODEL_NOT_EXISTS, modelCode);
        }
        String groovy = ruleModelDO.getGroovy();
        if (!StringUtils.hasText(groovy)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_MODEL_GROOVY_IS_NULL, modelCode);
        }
        ruleInfoDTO.setModelCode(modelCode);
        ruleInfoDTO.setModelGroovy(groovy);
    }

    /**
     * 设置 目标信息
     */
    private void setRuleTarget(RuleInfoDO ruleInfoDO, RuleInfoDTO ruleInfoDTO) {
        String targetCode = ruleInfoDO.getTargetCode();
        if (!StringUtils.hasText(targetCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_TARGET_CODE_IS_NULL);
        }
        RuleTargetDO ruleTargetDO = ruleTargetMapper.selectOneByTargetCode(targetCode);
        if (Objects.isNull(ruleTargetDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_TARGET_NOT_EXISTS, targetCode);
        }
        ruleInfoDTO.setTargetCode(ruleTargetDO.getTargetCode());
        ruleInfoDTO.setTargetField(ruleTargetDO.getTargetField());
        ruleInfoDTO.setTargetName(ruleTargetDO.getTargetName());
    }

    /**
     * 参数效验
     */
    private void validate(String ruleCode) {
        if (!StringUtils.hasText(ruleCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_CODE_NOT_BLANK);
        }
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        if (!Objects.equals(ruleInfoDO.getRuleStatus(), RuleStatusEnum.OFFLINE.getCode())
                && !Objects.equals(ruleInfoDO.getRuleStatus(), RuleStatusEnum.DRAFT.getCode())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_STATUS_NOT_OFFLINE_OR_DRAFT, ruleCode);
        }
    }

    /**
     * 设置目标信息
     */
    private void detailSetRuleTarget(RuleInfoRespVO ruleInfoRespVO) {
        String targetCode = ruleInfoRespVO.getTargetCode();
        if (!StringUtils.hasText(targetCode)) {
            return;
        }
        RuleTargetDO ruleTargetDO = ruleTargetMapper.selectOneByTargetCode(targetCode);
        if (Objects.isNull(ruleTargetDO)) {
            return;
        }
        ruleInfoRespVO.setTargetCode(ruleTargetDO.getTargetCode());
        ruleInfoRespVO.setTargetField(ruleTargetDO.getTargetField());
        ruleInfoRespVO.setTargetName(ruleTargetDO.getTargetName());
    }

    /**
     * 设置模型信息
     */
    private void detailSetRuleModel(RuleInfoRespVO ruleInfoRespVO) {
        String modelCode = ruleInfoRespVO.getModelCode();
        if (!StringUtils.hasText(modelCode)) {
            return;
        }
        RuleModelDO ruleModelDO = ruleModelMapper.selectOneByModelCode(modelCode);
        if (Objects.isNull(ruleModelDO)) {
            return;
        }
        ruleInfoRespVO.setModelCode(ruleModelDO.getModelCode());
        ruleInfoRespVO.setModelName(ruleModelDO.getModelName());
    }

    /**
     * 设置条件组
     */
    private void detailSetRuleCondGroup(RuleInfoRespVO ruleInfoRespVO) {
        String ruleCode = ruleInfoRespVO.getRuleCode();
        if (!StringUtils.hasText(ruleCode)) {
            return;
        }
        List<RuleCondDO> ruleCondDOList = ruleCondMapper.selectListByRuleCode(ruleCode);
        if (CollectionUtils.isEmpty(ruleCondDOList)) {
            return;
        }
        List<RuleCondRespVO> ruleCondGroup = BeanUtils.toBean(ruleCondDOList, RuleCondRespVO.class);
        List<String> ruleEventCodeList = ruleCondGroup.stream().map(RuleCondRespVO::getEventCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleEventCodeList)) {
            ruleInfoRespVO.setRuleCondGroup(ruleCondGroup);
            return;
        }
        // 给 条件组 设置 事件信息
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            ruleInfoRespVO.setRuleCondGroup(ruleCondGroup);
            return;
        }
        Map<String, RuleEventDO> eventCodeAndEventDoMap = ruleEventDOList.stream()
                .collect(Collectors.toMap(
                        RuleEventDO::getEventCode,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        ruleCondGroup.forEach(ruleCondRespVO -> {
            RuleEventDO ruleEventDO = eventCodeAndEventDoMap.get(ruleCondRespVO.getEventCode());
            ruleCondRespVO.setEventCode(ruleEventDO.getEventCode());
            ruleCondRespVO.setEventField(ruleEventDO.getEventField());
            ruleCondRespVO.setEventName(ruleEventDO.getEventName());
        });
        // 给 条件组 设置 事件属性值
        List<String> condCodeList = ruleCondGroup.stream().map(RuleCondRespVO::getCondCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(condCodeList)) {
            ruleInfoRespVO.setRuleCondGroup(ruleCondGroup);
            return;
        }
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = ruleEventAttrValueMapper.selectListByCondCodes(condCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrValueDOList)) {
            ruleInfoRespVO.setRuleCondGroup(ruleCondGroup);
            return;
        }
        List<RuleEventAttrValueRespVO> ruleEventAttrValueGroup = BeanUtils.toBean(ruleEventAttrValueDOList, RuleEventAttrValueRespVO.class);
        // 给 条件组 设置 事件属性
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            Map<String, List<RuleEventAttrValueRespVO>> condCodeAndAttrValueMap = ruleEventAttrValueGroup.stream()
                    .collect(Collectors.groupingBy(RuleEventAttrValueRespVO::getCondCode));
            ruleCondGroup.forEach(
                    ruleCondRespVO ->
                            ruleCondRespVO.setRuleEventAttrValueGroup(condCodeAndAttrValueMap.get(ruleCondRespVO.getCondCode()))
            );
            ruleInfoRespVO.setRuleCondGroup(ruleCondGroup);
            return;
        }
        Map<String, RuleEventAttrDO> attrCodeAndAttrDoMap = ruleEventAttrDOList.stream()
                .collect(Collectors.toMap(
                        RuleEventAttrDO::getAttrCode, Function.identity(),
                        (existing, replacement) -> existing
                ));
        ruleEventAttrValueGroup.forEach(ruleEventAttrValueRespVO -> {
            RuleEventAttrDO ruleEventAttrDO = attrCodeAndAttrDoMap.get(ruleEventAttrValueRespVO.getAttrCode());
            ruleEventAttrValueRespVO.setAttrField(ruleEventAttrDO.getAttrField());
            ruleEventAttrValueRespVO.setAttrName(ruleEventAttrDO.getAttrName());
            ruleEventAttrValueRespVO.setAttrType(ruleEventAttrDO.getAttrType());
        });
        Map<String, List<RuleEventAttrValueRespVO>> condCodeAndAttrValueMap = ruleEventAttrValueGroup.stream()
                .collect(Collectors.groupingBy(RuleEventAttrValueRespVO::getCondCode));
        ruleCondGroup.forEach(
                ruleCondRespVO ->
                        ruleCondRespVO.setRuleEventAttrValueGroup(condCodeAndAttrValueMap.get(ruleCondRespVO.getCondCode()))
        );
        // 给 规则 设置 条件组
        ruleInfoRespVO.setRuleCondGroup(ruleCondGroup);
    }
}
