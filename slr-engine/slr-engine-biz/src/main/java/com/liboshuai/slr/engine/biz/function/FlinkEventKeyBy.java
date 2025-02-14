package com.liboshuai.slr.engine.biz.function;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import org.apache.flink.api.java.functions.KeySelector;

/**
 * 实现KeySelector接口，用于从FlinkEventDTO对象中提取键
 * 这个类主要用于Flink框架中，定义如何从事件数据中选取键，以便进行后续的处理
 */
public class FlinkEventKeyBy implements KeySelector<FlinkEventDTO, String> {
    @Override
    public String getKey(FlinkEventDTO flinkEventDTO) {
        String channel = flinkEventDTO.getChannel();
        String targetField = flinkEventDTO.getTargetField();
        String targetValue = flinkEventDTO.getTargetValue();
        return String.join(RedisKeyConstants.REDIS_KEY_SPLIT, channel, targetField, targetValue);
    }
}
