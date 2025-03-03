package com.liboshuai.slr.engine.biz.processor;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.FlinkResultDTO;
import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.KeyedStateStore;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 运算机通用接口
 */
public interface Processor {

    /**
     * 初始化
     */
    void init(RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, RuleInfoDTO ruleInfoDTO) throws Exception;

    /**
     * 处理单条数据
     */
    boolean processElement(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp, FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out) throws Exception;

    /**
     * 定时器
     */
    boolean onTimer(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp, Collector<FlinkResultDTO> out) throws Exception;
}
