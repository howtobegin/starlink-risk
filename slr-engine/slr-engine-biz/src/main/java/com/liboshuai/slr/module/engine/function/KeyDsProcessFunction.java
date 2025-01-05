package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import com.liboshuai.slr.module.engine.dto.*;
import com.liboshuai.slr.module.engine.framework.exception.BusinessException;
import com.liboshuai.slr.module.engine.utils.CollectionUtil;
import com.liboshuai.slr.module.engine.utils.JsonUtil;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class KeyDsProcessFunction extends ProcessFunction<RuleCdcDTO, KafkaEventDTO> {

    private final ParameterTool parameterTool;

    public KeyDsProcessFunction(ParameterTool parameterTool) {
        this.parameterTool = parameterTool;
    }

    @Override
    public void processElement(RuleCdcDTO ruleCdcDTO, ProcessFunction<RuleCdcDTO, KafkaEventDTO>.Context context,
                               Collector<KafkaEventDTO> collector) throws Exception {
        if (ruleCdcDTO == null) {
            throw new BusinessException("Mysql Cdc 规则流 ruleCdcDTO 必须非空");
        }
        // cdc 数据变更类型
        String op = ruleCdcDTO.getOp();
        // 下线规则运算机
        if (Envelope.Operation.DELETE.code().equals(op)) {
            // 变更之前的数据
            RuleJsonDTO ruleCdcDTOBefore = ruleCdcDTO.getBefore();
            String ruleJsonBefore = ruleCdcDTOBefore.getRuleJson();
            RuleInfoDTO ruleInfoDTOBefore = JsonUtil.parseObject(ruleJsonBefore, RuleInfoDTO.class);
            if (Objects.isNull(ruleInfoDTOBefore)) {
                throw new BusinessException("Mysql Cdc 规则流 ruleCdcDTOBefore 必须非空");
            }
            Long ruleCode = ruleInfoDTOBefore.getRuleCode();
            Long ruleVersion = ruleInfoDTOBefore.getRuleVersion();
            String targetField = ruleInfoDTOBefore.getTargetField();
            String table = parameterTool.get(ParameterConstants.DORIS_TABLE_KEY);
            String sql = String.format("SELECT TARGET_VALUE FROM %s WHERE RULE_CODE = ? and RULE_VERSION = ? and TARGET_FIELD = ?", table);
//            List<String> targetValueList = DorisUtil.queryForList(sql, new DorisUtil.BeanPropertyRowMapper<>(String.class), ruleCode, ruleVersion, targetField);
            List<String> targetValueList = new ArrayList<>();
            if (CollectionUtil.isEmptyOrContainsNulls(targetValueList)) {
                log.warn("查询规则状态历史key值targetValue为空！");
                return;
            }
            for (String targetValue : targetValueList) {
                RuleKeyHistoryDTO ruleKeyHistoryDTO = RuleKeyHistoryDTO.builder()
                        .ruleCode(ruleCode)
                        .ruleVersion(ruleVersion)
                        .targetField(targetField)
                        .targetValue(targetValue)
                        .build();
                KafkaEventDTO kafkaEventDTO = KafkaEventDTO.builder()
                        .targetField(targetField)
                        .targetValue(targetValue)
                        .ruleKeyHistoryDTO(ruleKeyHistoryDTO)
                        .build();
                log.warn("生成的旧状态值清洗流数据：{}", kafkaEventDTO);
                collector.collect(kafkaEventDTO);
            }
        }
    }
}
