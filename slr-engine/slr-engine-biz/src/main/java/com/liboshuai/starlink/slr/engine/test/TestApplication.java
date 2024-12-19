package com.liboshuai.starlink.slr.engine.test;

import com.liboshuai.starlink.slr.engine.common.ParameterConstants;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Map;

/**
 * Flink 有状态的单词统计示例，演示普通变量和状态变量的区别
 */
public class TestApplication {

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port",8990);
        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        env.setParallelism(2);

        DataStreamSource<String> socketTextStream1 = env.socketTextStream("localhost", 10001);
        BroadcastStream<String> broadcastStream = socketTextStream1.broadcast(
                TestStateDescContainer.BROADCAST_MAP_STATE_DESC
        );
        KeyedStream<Tuple3<String, String, Integer>, String> tuple3StringKeyedStream =
                env.socketTextStream("localhost", 10002)
                .assignTimestampsAndWatermarks(WatermarkStrategy.noWatermarks())
                .flatMap(new LineSplitter())
                .keyBy(value -> value.f0);
        SingleOutputStreamOperator<Map<String, Long>> warnMessageStream = tuple3StringKeyedStream
                .connect(broadcastStream)
                .process(new TestCoreFunction());
        warnMessageStream.print();
        env.execute();
    }


}

