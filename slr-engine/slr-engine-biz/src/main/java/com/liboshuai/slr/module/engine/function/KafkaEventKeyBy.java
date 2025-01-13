package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.apache.flink.api.java.functions.KeySelector;

/**
 * 实现KeySelector接口，用于从KafkaEventDTO对象中提取键
 * 这个类主要用于Flink框架中，定义如何从事件数据中选取键，以便进行后续的处理
 */
public class KafkaEventKeyBy implements KeySelector<KafkaEventDTO, String> {
    @Override
    public String getKey(KafkaEventDTO kafkaEventDTO) throws Exception {
//        return kafkaEventDTO.getChannel()
//                + DefaultConstants.COLON + kafkaEventDTO.getTargetField()
//                + DefaultConstants.COLON + kafkaEventDTO.getTargetValue();
        String channel = kafkaEventDTO.getChannel();
        String targetField = kafkaEventDTO.getTargetField();
        String targetValue = kafkaEventDTO.getTargetValue();
        return String.join(DefaultConstants.COLON, channel, targetField, targetValue);
    }
}
