package com.liboshuai.slr.framework.common.convert;

import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

public class LocalDateTimeMapper {
    public LocalDateTime asLocalDateTime(String string) {
        if (!StringUtils.hasText(string)) {
            return null;
        }
        return LocalDateTimeUtils.convertStr2LocalDateTime(string);
    }

    public String asString(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        return LocalDateTimeUtils.convertLocalDateTime2Str(localDateTime);
    }
}
