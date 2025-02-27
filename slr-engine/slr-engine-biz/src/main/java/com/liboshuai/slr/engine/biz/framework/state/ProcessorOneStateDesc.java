package com.liboshuai.slr.engine.biz.framework.state;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * 运算机one的状态描述
 **/
public class ProcessorOneStateDesc {

    public static MapStateDescriptor<String, Tuple3<Long, FlinkEventDTO, Long>> getSmallMapStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "smallMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(stateDescName, Types.STRING, Types.TUPLE(Types.LONG, Types.POJO(FlinkEventDTO.class), Types.LONG));
    }

    public static MapStateDescriptor<String, Boolean> getSmallInitMapStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "smallInitMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(stateDescName, Types.STRING, Types.BOOLEAN);
    }

    public static ValueStateDescriptor<Boolean> getHasValueStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "hasValueState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(stateDescName, Types.BOOLEAN);
    }

    public static MapStateDescriptor<String, Boolean> getInTimeRangeStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "inTimeRangeState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(stateDescName, Types.STRING, Types.BOOLEAN);
    }

    public static ValueStateDescriptor<Long> getNextEndTimestampStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "nextEndTimestampState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(stateDescName, Types.LONG);
    }

    public static ValueStateDescriptor<FlinkEventDTO> getLastEventStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "lastEventState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(stateDescName, Types.POJO(FlinkEventDTO.class));
    }

    public static ValueStateDescriptor<Long> getLastAlertTimeStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "lastAlertTimeState_" + ruleCode + "_" + ruleVersion;
        return new ValueStateDescriptor<>(stateDescName, Types.LONG);
    }

    public static MapStateDescriptor<String, Long> getLatestEventThresholdMapStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "latestEventThresholdMapStateName_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(stateDescName, Types.STRING, Types.LONG);
    }

    public static MapStateDescriptor<Tuple2<String, Long>, Long> getGigMapStateDesc(Long ruleCode, Long ruleVersion) {
        String stateDescName = "bigMapState_" + ruleCode + "_" + ruleVersion;
        return new MapStateDescriptor<>(stateDescName, Types.TUPLE(Types.STRING, Types.LONG), Types.LONG);
    }
}
