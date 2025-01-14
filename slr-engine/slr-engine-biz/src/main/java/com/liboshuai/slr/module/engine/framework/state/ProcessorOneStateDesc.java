package com.liboshuai.slr.module.engine.framework.state;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * 运算机one的状态描述
 **/
public class ProcessorOneStateDesc {

    public static MapStateDescriptor<String, Boolean> getSmallInitMapStateDesc(Long ruleCode, Long ruleVersion) {
        String smallInitMapStateName = "smallInitMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(smallInitMapStateName, Types.STRING, Types.BOOLEAN);
    }

    public static ValueStateDescriptor<Long> getLastWarningTimeStateDesc(Long ruleCode, Long ruleVersion) {
        String lastWarningTimeStateName = "lastWarningTimeState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(lastWarningTimeStateName, Types.LONG);
    }

    public static MapStateDescriptor<String, Long> getLatestEventThresholdMapStateDesc(Long ruleCode, Long ruleVersion) {
        String latestEventThresholdMapStateName = "latestEventThresholdMapStateName_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(latestEventThresholdMapStateName, Types.STRING, Types.LONG);
    }

    public static MapStateDescriptor<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> getGigMapStateDesc(Long ruleCode, Long ruleVersion) {
        String bigMapStateName = "bigMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(bigMapStateName, Types.TUPLE(Types.STRING, Types.LONG), Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class)));
    }

}
