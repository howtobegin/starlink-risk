package com.liboshuai.slr.engine.biz.function;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.NginxEventDTO;
import com.liboshuai.slr.engine.biz.convert.EventConvert;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.util.StringUtils;

import java.util.Objects;

/**
 * json转flinkEventDto对象
 */
@Slf4j
public class Json2FlinkEventDtoMapFunction extends RichMapFunction<String, FlinkEventDTO> {
    @Override
    public FlinkEventDTO map(String s) throws Exception {
        NginxEventDTO nginxEventDTO = JsonUtils.parseObject(s, NginxEventDTO.class);
        if (Objects.isNull(nginxEventDTO)) {
            return null;
        }
        FlinkEventDTO flinkEventDTO = EventConvert.INSTANCE.nginxDto2FlinkDto(nginxEventDTO);
        String eventTime = nginxEventDTO.getEventTime();
        if (StringUtils.isNullOrWhitespaceOnly(eventTime)) {
            return flinkEventDTO;
        }
        try {
            flinkEventDTO.setEventTime(Long.parseLong(eventTime.replace(".", "")));
        } catch (NumberFormatException e) {
            log.warn("nginx事件时间转long类型失败，此数据忽略！nginxEventDTO: {}", JsonUtils.toJsonString(nginxEventDTO));
            return null;
        }
        return flinkEventDTO;
    }
}
