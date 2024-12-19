package com.liboshuai.starlink.slr.engine.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;

@Slf4j
public class LineSplitter implements FlatMapFunction<String, Tuple3<String, String, Integer>> {
    @Override
    public void flatMap(String s, Collector<Tuple3<String, String, Integer>> collector) throws Exception {
        String[] strings = s.split(" ");
        if (strings.length != 3) {
            log.warn("一行的元素个数不等于三");
        } else {
            collector.collect(new Tuple3<>(strings[0], strings[1], Integer.parseInt(strings[2])));
        }
    }
}
