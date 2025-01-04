package com.liboshuai.slr.module.engine.processor;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.ResultDTO;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.util.Collector;

/**
 * 运算机通用接口
 */
public interface Processor {

    /**
     * 初始化
     */
    void init(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception;

    /**
     * 处理单条数据
     */
    void processElement(long timestamp, RuleInfoDTO ruleInfoDTO, KafkaEventDTO kafkaEventDTO, Collector<ResultDTO> out) throws Exception;

    /**
     * 定时器
     */
    boolean onTimer(long timestamp, String currentKey, RuleInfoDTO ruleInfoDTO, Collector<ResultDTO> out) throws Exception;
}
