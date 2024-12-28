package com.liboshuai.slr.module.engine.utils;

/**
 * WindowUtil 类提供了用于计算时间窗口起始点的实用方法。
 */
public class WindowUtil {

    /**
     * 计算给定时间戳对应的窗口起始时间点，根据偏移量和窗口大小进行调整。
     *
     * @param timestamp  当前的时间戳（单位：毫秒）
     * @param offset     时间偏移量（单位：毫秒），用于调整窗口的起始点
     * @param windowSize 窗口的大小（单位：毫秒），定义每个时间窗口的持续时间
     * @return 计算得到的窗口起始时间戳（单位：毫秒）
     */
    public static long getWindowStartWithOffset(long timestamp, long offset, long windowSize) {
        // 计算时间戳减去偏移量后对窗口大小的余数
        final long remainder = (timestamp - offset) % windowSize;

        // 处理余数为负数的情况，以确保窗口起始点正确
        if (remainder < 0) {
            // 如果余数为负，调整时间戳以获取正确的窗口起始点
            return timestamp - (remainder + windowSize);
        } else {
            // 如果余数为正或零，直接从时间戳中减去余数得到窗口起始点
            return timestamp - remainder;
        }
    }
}

