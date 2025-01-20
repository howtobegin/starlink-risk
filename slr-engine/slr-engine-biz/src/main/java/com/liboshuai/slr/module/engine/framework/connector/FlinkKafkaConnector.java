package com.liboshuai.slr.module.engine.framework.connector;

import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

/**
 * author: liboshuai
 * description: Flink 读写Kafka工具类
 * date: 2023
 */
public class FlinkKafkaConnector {

    /**
     * author: liboshuai
     * description: 添加Source
     *
     * @param env: Flink上下文环境
     */
    public static DataStream<String> read(StreamExecutionEnvironment env,
                                          ParameterTool parameterTool) {

        String brokers = parameterTool.get(ParameterConstants.KAFKA_SOURCE_BROKERS);
        String topic = parameterTool.get(ParameterConstants.KAFKA_SOURCE_TOPIC);
        String group = parameterTool.get(ParameterConstants.KAFKA_SOURCE_GROUP);

        KafkaSource<String> KAFKA_SOURCE = KafkaSource.<String>builder()
                .setBootstrapServers(brokers)
                .setTopics(topic)
                .setGroupId(group)
                .setClientIdPrefix("slr-engine-")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        return env.fromSource(
                KAFKA_SOURCE,
                WatermarkStrategy.noWatermarks(),
                "kafka-[" + topic + "]"
        ).returns(Types.STRING).uid("kafka-source-" + topic);
    }

    public static void writer(DataStream<String> dataStream, ParameterTool parameterTool) {
        String brokers = parameterTool.get(ParameterConstants.KAFKA_SINK_BROKERS);
        String topic = parameterTool.get(ParameterConstants.KAFKA_SINK_TOPIC_ALERT);
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 10 * 60 * 1000 + "");
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(brokers) //broker地址
                .setKafkaProducerConfig(properties) //设置额外的参数
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.<String>builder()
                                .setTopic(topic) //topic
                                .setValueSerializationSchema(new SimpleStringSchema()) //设置value的序列化器
                                .build())
                //设置事务前缀
                .setTransactionalIdPrefix("starlink-risk-" + topic)
                //设置交付保证-至少一次
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
        dataStream.sinkTo(kafkaSink).name("kafka-[" + topic + "]").uid("kafka-[" + topic + "]");
    }

}
