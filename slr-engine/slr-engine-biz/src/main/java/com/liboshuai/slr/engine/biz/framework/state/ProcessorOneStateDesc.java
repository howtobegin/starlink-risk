package com.liboshuai.slr.engine.biz.framework.state;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * 运算机one的状态描述
 **/
public class ProcessorOneStateDesc {

    public static MapStateDescriptor<Tuple2<String, Long>, Tuple2<Long, FlinkEventDTO>> getSmallMapStateDesc(Long ruleCode, Long ruleVersion) {
        String smallMapStateName = "smallMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(smallMapStateName, Types.TUPLE(Types.STRING, Types.LONG), Types.TUPLE(Types.LONG, Types.POJO(FlinkEventDTO.class)));
    }

    public static MapStateDescriptor<String, Boolean> getSmallInitMapStateDesc(Long ruleCode, Long ruleVersion) {
        String smallInitMapStateName = "smallInitMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(smallInitMapStateName, Types.STRING, Types.BOOLEAN);
    }

    public static ValueStateDescriptor<Boolean> getHasValueStateDesc(Long ruleCode, Long ruleVersion) {
        String hasValueStateName = "hasValueState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(hasValueStateName, Types.BOOLEAN);
    }

    public static MapStateDescriptor<String, Boolean> getInTimeRangeStateDesc(Long ruleCode, Long ruleVersion) {
        String isTimeRangeStateName = "inTimeRangeState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(isTimeRangeStateName, Types.STRING, Types.BOOLEAN);
    }

    public static ValueStateDescriptor<Long> getNextEndTimestampStateDesc(Long ruleCode, Long ruleVersion) {
        String nextEndTimestampStateName = "nextEndTimestampState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(nextEndTimestampStateName, Types.LONG);
    }

    public static ValueStateDescriptor<FlinkEventDTO> getLastEventStateDesc(Long ruleCode, Long ruleVersion) {
        String lastEventStateName = "lastEventState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(lastEventStateName, Types.POJO(FlinkEventDTO.class));
    }

    public static ValueStateDescriptor<Long> getLastWarningTimeStateDesc(Long ruleCode, Long ruleVersion) {
        String lastWarningTimeStateName = "lastWarningTimeState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(lastWarningTimeStateName, Types.LONG);
    }

    public static MapStateDescriptor<String, Long> getLatestEventThresholdMapStateDesc(Long ruleCode, Long ruleVersion) {
        String latestEventThresholdMapStateName = "latestEventThresholdMapStateName_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(latestEventThresholdMapStateName, Types.STRING, Types.LONG);
    }

    public static MapStateDescriptor<Tuple2<String, Long>, Long> getGigMapStateDesc(Long ruleCode, Long ruleVersion) {
        String bigMapStateName = "bigMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(bigMapStateName, Types.TUPLE(Types.STRING, Types.LONG), Types.LONG);
    }
}
