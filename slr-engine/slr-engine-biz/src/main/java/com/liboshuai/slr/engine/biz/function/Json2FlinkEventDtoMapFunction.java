package com.liboshuai.slr.engine.biz.function;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.slr.engine.biz.convert.EventConvert;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import org.apache.flink.api.common.functions.RichMapFunction;

/**
 * json转flinkEventDto对象
 */
public class Json2FlinkEventDtoMapFunction extends RichMapFunction<String, FlinkEventDTO> {
    @Override
    public FlinkEventDTO map(String s) throws Exception {
        KafkaEventDTO kafkaEventDTO = JsonUtils.parseObject(s, KafkaEventDTO.class);
        return EventConvert.INSTANCE.kafkaDto2FlinkDto(kafkaEventDTO);
    }
}
