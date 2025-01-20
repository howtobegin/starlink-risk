package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.dynamic.datasource.annotation.Slave;
import com.liboshuai.slr.framework.common.constants.CacheKeyConstants;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import com.liboshuai.slr.framework.common.enums.CommonAuditOpEnum;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.framework.redis.core.manager.MultilevelCache;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdGenerator;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.*;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleCondRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventAttrValueRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.convert.riskRule.DorisEventConvert;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.*;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.*;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import com.liboshuai.slr.module.connector.api.alertMessage.AlertMessageApi;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.RuleCondDTO;
import com.liboshuai.slr.module.engine.dto.RuleEventAttrValueDTO;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import com.liboshuai.slr.module.engine.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleInfoServiceImpl implements RuleInfoService {
    private final RuleInfoMapper ruleInfoMapper;
    private final RuleModelMapper ruleModelMapper;
    private final RuleCondMapper ruleCondMapper;
    private final RuleEventMapper ruleEventMapper;
    private final RuleEventAttrMapper ruleEventAttrMapper;
    private final RuleEventAttrValueMapper ruleEventAttrValueMapper;
    private final RuleTargetMapper ruleTargetMapper;
    private final RuleJsonMapper ruleJsonMapper;
    private final DorisEventMapper dorisEventMapper;
    private final DorisEventConvert dorisEventConvert;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final AlertMessageApi alertMessageApi;
    private final MultilevelCache multilevelCache;

    @Override
    public PageResult<RuleInfoRespVO> page(RuleInfoPageReqVO ruleInfoPageReqVO) {
        PageResult<RuleInfoDO> ruleInfoEntityPageResult = ruleInfoMapper.selectPage(ruleInfoPageReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleInfoRespVO.class);
    }

    @Override
    public RuleInfoRespVO detail(Long ruleCode) {
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
    public Long create(RuleInfoSaveReqVO ruleInfoSaveReqVO) {

        // 保存 规则信息
        long ruleCode = snowflakeIdGenerator.nextId();
        ruleInfoSaveReqVO.setRuleCode(ruleCode); // 生成 规则编号
        ruleInfoSaveReqVO.setRuleStatus(CommonStatusEnum.DRAFT.getCode());  // 设置 初始规则状态
        ruleInfoSaveReqVO.setRuleVersion(0L);
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
        Long ruleCode = ruleInfoSaveReqVO.getRuleCode();
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
        // 更新缓存
        putCacheRuleInfo(ruleCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(RuleInfoChangeStatusReqVO ruleInfoChangeStatusReqVO) {
        Long ruleCode = ruleInfoChangeStatusReqVO.getRuleCode();
        String auditOp = ruleInfoChangeStatusReqVO.getAuditOp();
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        String ruleStatus = ruleInfoDO.getRuleStatus();
        String newRuleStatus = ruleInfoChangeStatusReqVO.getNewRuleStatus();
        if (Objects.equals(newRuleStatus, CommonStatusEnum.ONLINE_PENDING.getCode())) {
            // 进行上线操作
            if (!Objects.equals(ruleStatus, CommonStatusEnum.DRAFT.getCode())
                    && !Objects.equals(ruleStatus, CommonStatusEnum.OFFLINE.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_DRAFT);
            }
            ruleInfoDO.setRuleStatus(newRuleStatus);
        } else if (Objects.equals(newRuleStatus, CommonStatusEnum.ONLINE.getCode())) {
            // 进行上线审核操作
            if (!Objects.equals(ruleStatus, CommonStatusEnum.ONLINE_PENDING.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_ONLINE_PENDING);
            }
            if (Objects.equals(auditOp, CommonAuditOpEnum.APPROVE.getCode())) { // 审核通过
                ruleInfoDO.setRuleStatus(newRuleStatus);
            } else if (Objects.equals(auditOp, CommonAuditOpEnum.REJECT.getCode())) { // 审核拒绝
                ruleInfoDO.setRuleStatus(CommonStatusEnum.DRAFT.getCode());
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.AUDIT_OP_NOT_SUPPORT);
            }
            // 将规则数据存入 rule_json 表
            saveToRuleJson(ruleCode, ruleInfoDO);
        } else if (Objects.equals(newRuleStatus, CommonStatusEnum.OFFLINE_PENDING.getCode())) {
            // 进行下线操作
            if (!Objects.equals(ruleStatus, CommonStatusEnum.ONLINE.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_ONLINE);
            }
            ruleInfoDO.setRuleStatus(newRuleStatus);
        } else if (Objects.equals(newRuleStatus, CommonStatusEnum.OFFLINE.getCode())) {
            // 进行下线审核操作
            if (!Objects.equals(ruleStatus, CommonStatusEnum.OFFLINE_PENDING.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_OFFLINE_PENDING);
            }
            if (Objects.equals(auditOp, CommonAuditOpEnum.APPROVE.getCode())) { // 审核通过
                ruleInfoDO.setRuleStatus(newRuleStatus);
            } else if (Objects.equals(auditOp, CommonAuditOpEnum.REJECT.getCode())) { // 审核拒绝
                ruleInfoDO.setRuleStatus(CommonStatusEnum.ONLINE.getCode());
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.AUDIT_OP_NOT_SUPPORT);
            }
            // 将规则数据从 rule_json 表删除
            ruleJsonMapper.delete(RuleJsonDO::getRuleCode, ruleCode);
        } else {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.NEW_STATUS_NOT_SUPPORT);
        }
        ruleInfoMapper.updateByRuleCode(ruleInfoDO, ruleCode);
        // 更新缓存
        putCacheRuleInfo(ruleCode);
    }

    /**
     * 将规则数据存入 rule_json 表
     */
    private void saveToRuleJson(Long ruleCode, RuleInfoDO ruleInfoDO) {
        Long count = ruleJsonMapper.selectCount(RuleJsonDO::getRuleCode, ruleCode);
        if (Objects.isNull(count)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_JSON_COUNT_SELECT_ERROR, ruleCode);
        }
        if (count > 0) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_JSON_EXISTS, ruleCode);
        }
        // 版本号+1
        ruleInfoDO.setRuleVersion(ruleInfoDO.getRuleVersion() + 1);
        // 构建规则信息DTO
        RuleInfoDTO ruleInfoDTO = buildRuleInfoDTO(ruleCode);
        RuleJsonDO ruleJsonDO = RuleJsonDO.builder().ruleCode(ruleCode).ruleJson(JSON.toJSONString(ruleInfoDTO)).build();
        ruleJsonMapper.insert(ruleJsonDO);
    }

    /**
     * 构建规则信息DTO
     */
    @Master
    @Override
    public RuleInfoDTO buildRuleInfoDTO(Long ruleCode) {
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

    private RuleInfoDTO setRuleCodeGroup(Long ruleCode, RuleInfoDTO ruleInfoDTO) {
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

    private void setRuleModel(Long ruleCode, RuleInfoDO ruleInfoDO, RuleInfoDTO ruleInfoDTO) {
        Long modelCode = ruleInfoDO.getModelCode();
        if (Objects.isNull(modelCode)) {
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
    private void validate(Long ruleCode) {
        if (Objects.isNull(ruleCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_CODE_NOT_BLANK);
        }
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        if (!Objects.equals(ruleInfoDO.getRuleStatus(), CommonStatusEnum.OFFLINE.getCode())
                && !Objects.equals(ruleInfoDO.getRuleStatus(), CommonStatusEnum.DRAFT.getCode())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_OFFLINE_OR_DRAFT);
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
        Long modelCode = ruleInfoRespVO.getModelCode();
        if (Objects.isNull(modelCode)) {
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
        Long ruleCode = ruleInfoRespVO.getRuleCode();
        if (Objects.isNull(ruleCode)) {
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

    @Override
    public void putCacheRuleInfo(Long ruleCode) {
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        RuleInfoDTO ruleInfoDTO = BeanUtils.toBean(ruleInfoDO, RuleInfoDTO.class);
        multilevelCache.put(CacheKeyConstants.RULE_INFO + RedisKeyConstants.REDIS_KEY_SPLIT + ruleCode, ruleInfoDTO);
    }

    @Override
    public RuleInfoDTO getCacheRuleInfo(Long ruleCode) {
        RuleInfoDTO ruleInfoDTO = multilevelCache.get(
                CacheKeyConstants.RULE_INFO + RedisKeyConstants.REDIS_KEY_SPLIT + ruleCode,
                RuleInfoDTO.class
        );
        if (Objects.nonNull(ruleInfoDTO)) {
            return ruleInfoDTO;
        }
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_INFO_NOT_EXISTS, ruleCode);
        }
        ruleInfoDTO = BeanUtils.toBean(ruleInfoDO, RuleInfoDTO.class);
        multilevelCache.put(CacheKeyConstants.RULE_INFO + RedisKeyConstants.REDIS_KEY_SPLIT + ruleCode, ruleInfoDTO);
        return ruleInfoDTO;
    }

    @Override
    public void refreshCache() {
        List<RuleInfoDO> ruleInfoDOList = ruleInfoMapper.selectList();
        if (CollectionUtils.isEmpty(ruleInfoDOList)) {
            return;
        }
        ruleInfoDOList.forEach(ruleInfoDO ->
                multilevelCache.put(
                        CacheKeyConstants.RULE_INFO + RedisKeyConstants.REDIS_KEY_SPLIT + ruleInfoDO.getRuleCode(),
                        BeanUtils.toBean(ruleInfoDO, RuleInfoDTO.class)
                )
        );
    }

    /**
     * 验证flink规则运算是否正确
     * 通过查询doris事件表，并进行模拟滑动窗口计算得到预警信息，然后对比mongo中的预警信息，以此验证flink规则运算是否正确
     * （目前仅支持单条件、无事件属性的规则验证）
     *
     * @param ruleCode 规则编号
     * @return true:正确，false:错误
     */
    @Slave
    @Override
    public Boolean validateFlink(Long ruleCode) {
        // 通过规则编号构建RuleInfoDTO对象
        RuleInfoService ruleInfoService = (RuleInfoService) AopContext.currentProxy();
        RuleInfoDTO ruleInfoDTO = ruleInfoService.buildRuleInfoDTO(ruleCode);
        // 获取RuleInfoDTO对象中的各个属性
        String channel = ruleInfoDTO.getChannel();
        String targetField = ruleInfoDTO.getTargetField();
        // 获取规则告警间隔时间
        Long alertInterval = getAlertInterval(ruleInfoDTO);
        // 获取规则条件组
        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup();
        if (CollectionUtils.isEmpty(ruleCondGroup)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_COND_GROUP_IS_NULL);
        }
        // 目前仅支持单条件规则
        if (ruleCondGroup.size() > 1) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.ONLY_SUPPORT_SINGLE_COND_RULE);
        }
        RuleCondDTO ruleCondDTO = ruleCondGroup.get(0);
        List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = ruleCondDTO.getRuleEventAttrValueGroup();
        if (!CollectionUtils.isEmpty(ruleEventAttrValueGroup)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.ONLY_SUPPORT_NULL_ATTR_RULE);
        }
        // 获取事件字段
        String eventField = ruleCondDTO.getEventField();
        // 获取窗口大小与窗口步长（毫秒）
        long windowSize = TimeUtil.toMillis(ruleCondDTO.getWindowValue(), TimeUnitEnum.fromEnUnit(ruleCondDTO.getWindowUnit()));
        long windowStep = TimeUnit.MINUTES.toMillis(1); // 窗口步长恒定为1分钟
        // 根据渠道、目标字段、事件字段查询doris历史事件数据
        List<DorisEventDO> dorisEventDOList = dorisEventMapper.selectListByKey(channel, targetField, eventField);
        if (CollectionUtils.isEmpty(dorisEventDOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.DORIS_HISTORY_EVENT_IS_NULL);
        }
        // 当最后一条数据的窗口结束了，才能进行验证
        DorisEventDO dorisEventDO = dorisEventDOList.get(dorisEventDOList.size() - 1);
        LocalDateTime eventTime = dorisEventDO.getEventTime();
        long flinkProcessEndTime = LocalDateTimeUtils.convertLocalDateTime2Timestamp(eventTime) + windowSize;
        if (System.currentTimeMillis() <= flinkProcessEndTime) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.FLINK_PROCESS_NOT_END, LocalDateTimeUtils.convertTimestamp2String(flinkProcessEndTime));
        }
        // doris 事件数据转换成 kafka 事件数据
        List<KafkaEventDTO> kafkaEventDTOList = dorisEventConvert.batchConvertDO2KafkaDTO(dorisEventDOList);
        // 根据 targetValue 进行分组
        Map<String, List<KafkaEventDTO>> targetValueAndKafkaEventDtoMap = kafkaEventDTOList.stream()
                .collect(Collectors.groupingBy(KafkaEventDTO::getTargetValue));
        // 最终生成的风控信息集合
        List<AlertMessageApiDTO> alertMessageApiDTOS = new ArrayList<>();
        // 遍历每个targetValue下的数据，进行风控规则判断
        process(ruleCode, targetValueAndKafkaEventDtoMap, windowSize, windowStep, ruleCondDTO, alertInterval, channel, targetField, eventField, alertMessageApiDTOS);
        // 查询mongo中对应规则产生的预警信息
        List<AlertMessageApiDTO> mongoAlertMessageApiDtoList = alertMessageApi.findByRuleCode(ruleCode);
        if (CollectionUtils.isEmpty(alertMessageApiDTOS) && CollectionUtils.isEmpty(mongoAlertMessageApiDtoList)) {
            log.info("计算得出的预警信息条数与mongo中的预警信息条数都为0");
            return true;
        }
        if (CollectionUtils.isEmpty(alertMessageApiDTOS)) {
            log.info("计算得出的预警信息条数不为0，但mongo中的预警信息条数为0");
            return false;
        }
        if (CollectionUtils.isEmpty(mongoAlertMessageApiDtoList)) {
            log.info("计算得出的预警信息条数为0，但mongo中的预警信息条数不为0");
            return false;
        }
        int processSize = alertMessageApiDTOS.size();
        int mongoSize = mongoAlertMessageApiDtoList.size();
        log.info("计算得出/mongo中的预警信息条数分别为: {}, {}", processSize, mongoSize);
        // 对比'计算得出的预警信息'条件与'mongo中的预警数据'条数、内容是否一致
        return compareAlerts(alertMessageApiDTOS, mongoAlertMessageApiDtoList);
    }

    /**
     * 遍历每个targetValue下的数据，进行风控规则判断
     */
    private void process(Long ruleCode, Map<String, List<KafkaEventDTO>> targetValueAndKafkaEventDtoMap,
                         long windowSize, long windowStep, RuleCondDTO ruleCondDTO, Long alertInterval,
                         String channel, String targetField, String eventField, List<AlertMessageApiDTO> alertMessageApiDTOS) {
        for (Map.Entry<String, List<KafkaEventDTO>> entry : targetValueAndKafkaEventDtoMap.entrySet()) {
            String targetValue = entry.getKey();
            List<KafkaEventDTO> kafkaEventDTOS = entry.getValue();
            // 最近一次预警时间（针对当前 TARGET_VALUE）
            long lastAlertTimestamp = 0L;
            if (CollectionUtils.isEmpty(kafkaEventDTOS)) {
                continue;
            }
            // 上一次更新后的阈值
            Long latestThreshold = null;
            // 获取第一个和最后一个事件的时间戳
            KafkaEventDTO firstKafkaEventDO = kafkaEventDTOS.get(0);
            long firstEventTimestamp = firstKafkaEventDO.getEventTime();
            KafkaEventDTO latestKafkaEventDO = kafkaEventDTOS.get(kafkaEventDTOS.size() - 1);
            long latestEventTimestamp = latestKafkaEventDO.getEventTime();

            // 计算窗口的起始时间
            long earliestWindowStartTimeStamp = firstEventTimestamp - windowSize + windowStep;
            // 对齐到整分钟
            earliestWindowStartTimeStamp = earliestWindowStartTimeStamp - (earliestWindowStartTimeStamp % windowStep);
            long latestWindowStartTimeStamp = latestEventTimestamp - (latestEventTimestamp % windowStep);
            // 模拟滑动窗口
            for (long windowStartTimeStamp = earliestWindowStartTimeStamp;
                 windowStartTimeStamp <= latestWindowStartTimeStamp;
                 windowStartTimeStamp += windowStep) {
                // 更新窗口结束时间
                long windowEndTimeStamp = windowStartTimeStamp + windowSize;
                // 过滤出在当前窗口内的事件
                long finalWindowStartTimeStamp = windowStartTimeStamp;
                List<KafkaEventDTO> windowsKafkaEventDOList = kafkaEventDTOS.stream()
                        .filter(eventDO -> {
                            long eventTimestamp = eventDO.getEventTime();
                            return eventTimestamp >= finalWindowStartTimeStamp && eventTimestamp < windowEndTimeStamp;
                        })
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(windowsKafkaEventDOList)) {
                    continue;
                }
                // 计算事件值累计和
                long eventValueSum = windowsKafkaEventDOList.stream()
                        .mapToLong(eventDO -> Long.parseLong(eventDO.getEventValue()))
                        .sum();
                // 计算阈值
                // FIXME: 逻辑错误，应该对应条件预警触发后，才更新
                Long eventThreshold = ruleCondDTO.getThreshold();
                Long thresholdScaleFactor = ruleCondDTO.getThresholdScaleFactor();
                if (Objects.nonNull(thresholdScaleFactor)) {
                    if (Objects.nonNull(latestThreshold)) {
                        eventThreshold = latestThreshold * thresholdScaleFactor;
                    }
                    latestThreshold = eventThreshold;
                }
                // 判断是否需要预警
                boolean shouldAlert = (eventValueSum > eventThreshold) &&
                        (alertInterval == null || (windowEndTimeStamp - lastAlertTimestamp >= alertInterval));
                if (shouldAlert) {
                    AlertMessageApiDTO alertMessageApiDTO = AlertMessageApiDTO.builder()
                            .channel(channel)
                            .ruleCode(ruleCode)
                            .targetField(targetField)
                            .targetValue(targetValue)
                            .eventValueGroup(Collections.singletonMap(eventField, eventValueSum))
                            .alertTime(LocalDateTimeUtils.convertTimestamp2LocalDateTime(windowEndTimeStamp))
                            .build();
                    alertMessageApiDTOS.add(alertMessageApiDTO);
                    // 更新 lastAlertTimestamp 为当前窗口的结束时间
                    lastAlertTimestamp = windowEndTimeStamp;
                }
            }
        }
    }

    private Long getAlertInterval(RuleInfoDTO ruleInfoDTO) {
        Long alertIntervalValue = ruleInfoDTO.getAlertIntervalValue();
        String alertIntervalUnit = ruleInfoDTO.getAlertIntervalUnit();
        if (Objects.isNull(alertIntervalValue) || Objects.isNull(alertIntervalUnit)) {
            return null;
        }
        return TimeUtil.toMillis(alertIntervalValue, TimeUnitEnum.fromEnUnit(alertIntervalUnit));
    }

    private Boolean compareAlerts(List<AlertMessageApiDTO> generatedAlerts, List<AlertMessageApiDTO> mongoAlerts) {
        if (CollectionUtils.isEmpty(generatedAlerts) && CollectionUtils.isEmpty(mongoAlerts)) {
            log.info("计算得出的预警信息条数与Mongo中的预警信息条数都为0");
            return true;
        }
        if (CollectionUtils.isEmpty(generatedAlerts) || CollectionUtils.isEmpty(mongoAlerts)) {
            log.info("计算得出的预警信息条数与Mongo中的预警信息条数不一致。计算: {}, Mongo: {}", generatedAlerts.size(), mongoAlerts.size());
            return false;
        }
        if (generatedAlerts.size() != mongoAlerts.size()) {
            log.info("计算得出 / Mongo 中的预警信息条数不一致！计算: {}, Mongo: {}", generatedAlerts.size(), mongoAlerts.size());
            return false;
        }

        // 确保 AlertMessageDTO 重写了 equals 和 hashCode 方法
        List<AlertMessageApiDTO> sortedGeneratedAlerts = generatedAlerts.stream()
                .sorted(Comparator.comparing(AlertMessageApiDTO::getAlertTime))
                .collect(Collectors.toList());
        log.info("sortedGeneratedAlerts: {}", sortedGeneratedAlerts);
        List<AlertMessageApiDTO> sortedMongoAlerts = mongoAlerts.stream()
                .sorted(Comparator.comparing(AlertMessageApiDTO::getAlertTime))
                .collect(Collectors.toList());
        log.info("sortedMongoAlerts: {}", sortedMongoAlerts);
        for (int i = 0; i < sortedGeneratedAlerts.size(); i++) {
            AlertMessageApiDTO generatedAlert = sortedGeneratedAlerts.get(i);
            AlertMessageApiDTO mongoAlert = sortedMongoAlerts.get(i);
            mongoAlert.setAlertMessage(null);
            mongoAlert.setAlertTime(mongoAlert.getAlertTime().withSecond(0).withNano(0));
            if (!generatedAlert.equals(mongoAlert)) {
                log.info("预警信息内容不一致！计算: {}, Mongo: {}", generatedAlert, mongoAlert);
                return false;
            }
        }
        log.info("所有预警信息均与Mongo中的数据一致");
        return true;
    }
}
