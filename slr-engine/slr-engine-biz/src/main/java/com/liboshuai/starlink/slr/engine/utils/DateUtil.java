package com.liboshuai.starlink.slr.engine.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * author: liboshuai
 * description: 基于LocalDate封装的时间工具类
 * date: 2023
 */
public class DateUtil {

    //时间字符串的格式
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";


    /**
     * author: liboshuai
     * description: DateTimeFormatter设置时间格式
     *
     * @param :
     * @return java.time.format.DateTimeFormatter
     */
    private static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern(PATTERN);
    }

    /* **********************
     *
     * LocalDateTime 和 String 的互相转换
     *
     * *********************/

    /**
     * author: liboshuai
     * description: LocalDateTime转换为字符串
     *
     * @param dateTime:
     * @return java.lang.String
     */
    public static String convertLocalDateTime2Str(LocalDateTime dateTime) {
        DateTimeFormatter dtf = getFormatter();
        return dtf.format(dateTime);
    }


    /**
     * author: liboshuai
     * description: 字符串转换为LocalDateTime
     *
     * @param str:
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime convertStr2LocalDateTime(String str) {
        DateTimeFormatter dtf = getFormatter();
        return LocalDateTime.parse(str, dtf);
    }


    /* **********************
     *
     *
     * 时间戳 和 LocalDateTime 的互相转换
     *
     * *********************/

    /**
     * author: liboshuai
     * description: 时间戳转换为LocalDateTime
     *
     * @param timestamp:
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime convertTimestamp2LocalDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * author: liboshuai
     * description: LocalDateTime转换为时间戳
     *
     * @param dateTime:
     * @return long
     */
    public static long convertLocalDateTime2Timestamp(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 时间戳转字符串
     */
    public static String convertTimestamp2String(long timestamp) {
        LocalDateTime localDateTime = convertTimestamp2LocalDateTime(timestamp);
        return convertLocalDateTime2Str(localDateTime);
    }

    /**
     * 字符串转时间戳
     */
    public static long convertString2Timestamp(String str) {
        LocalDateTime localDateTime = convertStr2LocalDateTime(str);
        return convertLocalDateTime2Timestamp(localDateTime);
    }
}


