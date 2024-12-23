package com.liboshuai.starlink.slr.engine.function;

import com.liboshuai.starlink.slr.engine.api.constants.GlobalConstants;
import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import org.apache.flink.api.java.functions.KeySelector;

/**
 * 实现KeySelector接口，用于从KafkaEventDTO对象中提取键
 * 这个类主要用于Flink框架中，定义如何从事件数据中选取键，以便进行后续的处理
 */
public class KafkaEventKeyBy implements KeySelector<KafkaEventDTO, String> {
    @Override
    public String getKey(KafkaEventDTO kafkaEventDTO) throws Exception {
        return kafkaEventDTO.getKeyCode() + GlobalConstants.FLINK_KEY_SEPARATOR + kafkaEventDTO.getKeyValue();
    }
}
