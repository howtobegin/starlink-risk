package com.liboshuai.slr.engine.biz.function;

import com.liboshuai.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import org.apache.flink.api.java.functions.KeySelector;

/**
 * 实现KeySelector接口，用于从KafkaEventDTO对象中提取键
 * 这个类主要用于Flink框架中，定义如何从事件数据中选取键，以便进行后续的处理
 */
public class KafkaEventKeyBy implements KeySelector<KafkaEventDTO, String> {
    @Override
    public String getKey(KafkaEventDTO kafkaEventDTO) {
        String channel = kafkaEventDTO.getChannel();
        String targetField = kafkaEventDTO.getTargetField();
        String targetValue = kafkaEventDTO.getTargetValue();
        return String.join(DefaultConstants.COLON, channel, targetField, targetValue);
    }
}
