package com.liboshuai.slr.engine.biz.processor;

import com.liboshuai.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.slr.engine.api.dto.ResultDTO;
import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.KeyedStateStore;
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
    void processElement(String currentKey, long timestamp, KafkaEventDTO kafkaEventDTO, Collector<ResultDTO> out) throws Exception;

    /**
     * 定时器
     */
    boolean onTimer(String currentKey, long timestamp, Collector<ResultDTO> out) throws Exception;
}
