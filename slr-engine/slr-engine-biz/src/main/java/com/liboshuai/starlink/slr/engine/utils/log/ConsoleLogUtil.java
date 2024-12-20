package com.liboshuai.starlink.slr.engine.utils.log;

import com.liboshuai.starlink.slr.engine.enums.ConsoleLogLevelEnum;
import com.liboshuai.starlink.slr.engine.utils.string.JsonUtil;
import com.liboshuai.starlink.slr.engine.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @Author: liboshuai
 * @Date: 2023-10-17 18:25
 * 控制日志打印工具类
 **/
@Slf4j
public class ConsoleLogUtil {


    private static final String PROPERTIES_FILE_PATH = "flink.properties";
    private static ConsoleLogLevelEnum logLevel;

    static {
        try {
            Properties properties = new Properties();
            InputStream input = ConsoleLogUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_PATH);
            properties.load(input);
            String level = properties.getProperty("consoleLogUtil.level", "INFO");
            logLevel = ConsoleLogLevelEnum.fromString(level);
        } catch (IOException e) {
            logLevel = ConsoleLogLevelEnum.INFO; // Use INFO as default log level
        }
    }

    public static void debug(String pattern, Object... arguments) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        if (logLevel.ordinal() > ConsoleLogLevelEnum.DEBUG.ordinal()) {
            return;
        }
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String classMsg = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "." + stackTraceElement.getLineNumber();
        System.out.printf("%-26s", "[" + simpleDateFormat.format(new Date()) + "]");
        System.out.printf("%-8s", "DEBUG");
        System.out.printf("%-1s", "[" + classMsg + "]");
        System.out.print(" : " + StringUtil.format(pattern, arguments) + "\n");
    }

    public static void info(String pattern, Object... arguments) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        if (logLevel.ordinal() > ConsoleLogLevelEnum.INFO.ordinal()) {
            return;
        }
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String classMsg = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "." + stackTraceElement.getLineNumber();
        System.out.printf("%-26s", "[" + simpleDateFormat.format(new Date()) + "]");
        System.out.printf("%-8s", "INFO");
        System.out.printf("%-1s", "[" + classMsg + "]");
        System.out.print(" : " + StringUtil.format(pattern, arguments) + "\n");
    }

    public static void warning(String pattern, Object... arguments) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        if (logLevel.ordinal() > ConsoleLogLevelEnum.WARN.ordinal()) {
            return;
        }
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String classMsg = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "." + stackTraceElement.getLineNumber();
        System.out.printf("%-26s", "[" + simpleDateFormat.format(new Date()) + "]");
        System.out.printf("%-8s", "WARNING");
        System.out.printf("%-1s", "[" + classMsg + "]");
        System.out.print(" : " + StringUtil.format(pattern, arguments) + "\n");
    }

    public static void error(String pattern, Object... arguments) {
        if (logLevel.ordinal() > ConsoleLogLevelEnum.ERROR.ordinal()) {
            return;
        }
        splicingErrorLogs();
        System.out.print(" : " + StringUtil.format(pattern, arguments) + "\n");
    }

    private static void splicingErrorLogs() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String classMsg = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "." + stackTraceElement.getLineNumber();
        System.out.printf("%-26s", "[" + simpleDateFormat.format(new Date()) + "]");
        System.out.printf("%-8s", "ERROR");
        System.out.printf("%-1s", "[" + classMsg + "]");
    }

    /**
     * 同时将错误信息打印到log4j2与控制台上
     * @param pattern 异常文本
     * @param e 异常类
     */
    public static void log4j2Error(String pattern, Exception e) {
        if (logLevel.ordinal() > ConsoleLogLevelEnum.ERROR.ordinal()) {
            return;
        }
        splicingErrorLogs();
        System.out.print(" : " + StringUtil.format(pattern + ": \n{}", JsonUtil.toJsonString(e)) + "\n");
        log.error(pattern + ": ", e);
    }
}

