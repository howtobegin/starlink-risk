package com.liboshuai.slr.engine.biz.function;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.NginxEventDTO;
import com.liboshuai.slr.engine.biz.convert.EventConvert;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RichMapFunction;

/**
 * json转flinkEventDto对象
 */
@Slf4j
public class Json2FlinkEventDtoMapFunction extends RichMapFunction<String, FlinkEventDTO> {
    @Override
    public FlinkEventDTO map(String s) throws Exception {
        NginxEventDTO nginxEventDTO = JsonUtils.parseObject(s, NginxEventDTO.class);
        return EventConvert.INSTANCE.nginxDto2FlinkDto(nginxEventDTO);
    }
}
