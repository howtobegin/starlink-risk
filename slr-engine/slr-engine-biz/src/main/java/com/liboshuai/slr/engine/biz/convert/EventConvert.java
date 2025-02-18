package com.liboshuai.slr.engine.biz.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.liboshuai.slr.engine.api.dto.DorisEventDTO;
import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.NginxEventDTO;
import com.liboshuai.slr.engine.biz.framework.exception.EventConvertException;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import org.apache.flink.util.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface EventConvert {

    EventConvert INSTANCE = Mappers.getMapper(EventConvert.class);

    @Mapping(source = "channel", target = "channel", qualifiedByName = "deCodeString")
    @Mapping(source = "targetField", target = "targetField", qualifiedByName = "deCodeString")
    @Mapping(source = "targetValue", target = "targetValue", qualifiedByName = "deCodeString")
    @Mapping(source = "eventField", target = "eventField", qualifiedByName = "deCodeString")
    @Mapping(source = "eventValue", target = "eventValue", qualifiedByName = "deCodeString")
    @Mapping(source = "eventAttrMap", target = "eventAttrMap", qualifiedByName = "encodeStringToMap")
    FlinkEventDTO nginxDto2FlinkDto(NginxEventDTO nginxEventDTO);

    @Mapping(source = "eventTime", target = "eventTime", qualifiedByName = "dateTimeLongToString")
    DorisEventDTO flinkDto2DorisDto(FlinkEventDTO flinkEventDTO);

    @Named("deCodeString")
    default String deCodeString(String encoderString) {
        if (StringUtils.isNullOrWhitespaceOnly(encoderString)) {
            return null;
        }
        String value;
        try {
            value = URLDecoder.decode(encoderString, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new EventConvertException("解码字符串失败: ", e);
        }
        return value;
    }

    @Named("encodeStringToMap")
    default Map<String, String> encodeStringToMap(String eventAttrMapEnCoderStr) {
        if (StringUtils.isNullOrWhitespaceOnly(eventAttrMapEnCoderStr)) {
            return null;
        }
        Map<String, String> eventAttrMap;
        try {
            String eventAttrMapStr = URLDecoder.decode(eventAttrMapEnCoderStr, StandardCharsets.UTF_8.name());
            eventAttrMap = JsonUtils.parseObjectWithType(eventAttrMapStr,
                    new TypeReference<Map<String, String>>() {
                    });
        } catch (UnsupportedEncodingException e) {
            throw new EventConvertException("解码 eventAttrMap 失败: ", e);
        }
        return eventAttrMap;
    }

    @Named("dateTimeLongToString")
    default String dateTimeLongToString(Long eventTime) {
        if (Objects.isNull(eventTime)) {
            return null;
        }
        return LocalDateTimeUtils.convertTimestamp2String(eventTime);
    }
}
