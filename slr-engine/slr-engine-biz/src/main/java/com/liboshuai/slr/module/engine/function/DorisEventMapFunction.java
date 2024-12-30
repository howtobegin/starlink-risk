package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.module.engine.convert.EventConvert;
import com.liboshuai.slr.module.engine.dto.DorisEventDTO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.utils.JsonUtil;
import org.apache.flink.api.common.functions.MapFunction;

public class DorisEventMapFunction implements MapFunction<KafkaEventDTO, String> {
    @Override
    public String map(KafkaEventDTO kafkaEventDTO) throws Exception {
        DorisEventDTO dorisEventDTO = EventConvert.INSTANCE.kafkaDto2DorisDto(kafkaEventDTO);
        return JsonUtil.toJsonStringWithUpperSnakeCaseKeys(dorisEventDTO); // 转为大写下划线，适配doris表结构字段
    }
}
