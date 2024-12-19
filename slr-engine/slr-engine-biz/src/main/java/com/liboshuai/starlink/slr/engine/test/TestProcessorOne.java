package com.liboshuai.starlink.slr.engine.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class TestProcessorOne implements TestProcessor {

    private MapState<String, Long> mapState;

    @Override
    public void init(RuntimeContext runtimeContext, String ruleCode) throws IOException {
        mapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("mapState_" + ruleCode, String.class, Long.class)
        );
    }

    @Override
    public void processElement(long timestamp, Tuple3<String, String, Integer> tuple3, String ruleCode, Collector<Map<String, Long>> out) throws Exception {
        log.info("tuple3: {}", tuple3);
        if (!mapState.contains(tuple3.f1)) {
            mapState.put(tuple3.f1, 0L);
        }
        mapState.put(tuple3.f1, mapState.get(tuple3.f1) + tuple3.f2);
        log.info("mapState:{}", mapState);
    }

    @Override
    public void onTimer(
            long timestamp, String ruleCode, Collector<Map<String, Long>> out,
            KeyedBroadcastProcessFunction<String, Tuple3<String, String, Integer>, String, Map<String, Long>>.OnTimerContext ctx
    ) throws Exception {
        Map<String, Long> map = new HashMap<>();
        for (Map.Entry<String, Long> entry : mapState.entries()) {
            map.put(ctx.getCurrentKey() + "_" + entry.getKey(), entry.getValue());
        }
        out.collect(map);
    }
}
