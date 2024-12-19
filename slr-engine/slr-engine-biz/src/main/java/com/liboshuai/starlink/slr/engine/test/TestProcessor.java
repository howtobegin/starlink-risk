package com.liboshuai.starlink.slr.engine.test;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.Map;

/**
 * 运算机通用接口
 */
public interface TestProcessor {

    /**
     * 初始化
     */
    void init(RuntimeContext runtimeContext, String ruleCode) throws IOException;

    /**
     * 处理单条数据
     */
    void processElement(long timestamp, Tuple3<String, String, Integer> tuple3, String ruleCode, Collector<Map<String, Long>> out) throws Exception;

    /**
     * 定时器
     */
    void onTimer(
            long timestamp, String ruleCode, Collector<Map<String, Long>> out,
            KeyedBroadcastProcessFunction<String, Tuple3<String, String, Integer>, String, Map<String, Long>>.OnTimerContext ctx
    ) throws Exception;
}
