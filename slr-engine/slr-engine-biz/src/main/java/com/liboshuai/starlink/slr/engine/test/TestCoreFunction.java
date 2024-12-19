package com.liboshuai.starlink.slr.engine.test;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;

public class TestCoreFunction extends KeyedBroadcastProcessFunction<String, Tuple3<String, String, Integer>, String, Map<String, Long>> {

    private Map<String, TestProcessor> processorMap;

    @Override
    public void open(Configuration parameters) throws Exception {
        processorMap = new HashMap<>();
    }

    @Override
    public void processElement(
            Tuple3<String, String, Integer> stringStringIntegerTuple3,
            KeyedBroadcastProcessFunction<String, Tuple3<String, String, Integer>, String, Map<String, Long>>.ReadOnlyContext readOnlyContext,
            Collector<Map<String, Long>> collector
    ) throws Exception {
        long processTime = readOnlyContext.currentProcessingTime();
        for (Map.Entry<String, TestProcessor> entry : processorMap.entrySet()) {
            String ruleCode = entry.getKey();
            TestProcessor testProcessor = entry.getValue();
            testProcessor.processElement(processTime, stringStringIntegerTuple3, ruleCode, collector);
        }
        long fireTime = getWindowStartWithOffset(processTime, 0, 60 * 1000) + 60 * 1000;
        readOnlyContext.timerService().registerProcessingTimeTimer(fireTime);
    }

    @Override
    public void processBroadcastElement(
            String s,
            KeyedBroadcastProcessFunction<String, Tuple3<String, String, Integer>, String, Map<String, Long>>.Context context,
            Collector<Map<String, Long>> collector
    ) throws Exception {
        if (!processorMap.containsKey(s)) {
            TestProcessorOne testProcessorOne = new TestProcessorOne();
            testProcessorOne.init(getRuntimeContext(),s);
            processorMap.put(s, testProcessorOne);
        }
    }

    @Override
    public void onTimer(
            long timestamp,
            KeyedBroadcastProcessFunction<String, Tuple3<String, String, Integer>, String, Map<String, Long>>.OnTimerContext ctx,
            Collector<Map<String, Long>> out
    ) throws Exception {
        for (Map.Entry<String, TestProcessor> entry : processorMap.entrySet()) {
            String ruleCode = entry.getKey();
            TestProcessor testProcessor = entry.getValue();
            testProcessor.onTimer(timestamp, ruleCode, out, ctx);
        }
    }

    private long getWindowStartWithOffset(long timestamp, long offset, long windowSize) {
        final long remainder = (timestamp - offset) % windowSize;
        // handle both positive and negative cases
        if (remainder < 0) {
            return timestamp - (remainder + windowSize);
        } else {
            return timestamp - remainder;
        }
    }
}
