package com.liboshuai.slr.module.connector.convert;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LocalDateTimeMapper {

    public String asString(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return date != null ? date.format(formatter) : null;
    }

    public LocalDateTime asLocalDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return date != null ? LocalDateTime.parse(date, formatter) : null;
    }
}
