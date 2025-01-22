package com.liboshuai.slr.module.admin.util.mock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liboshuai.slr.module.admin.common.enums.mock.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

/**
 * 事件数据mock工具类
 */
public class MockEventUtils {

    private static final Random random = new Random();
    private static final List<String> userNameList;
    private static final Map<String, List<String>> userNameAddressMap;

    static  {
        userNameAddressMap = getUserNameAddressMap();
        Set<String> userNameSet = userNameAddressMap.keySet();
        userNameList = new ArrayList<>(userNameSet);
    }


    private static <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
        int x = random.nextInt(enumClass.getEnumConstants().length);
        return enumClass.getEnumConstants()[x];
    }


    /**
     * 随机获取userName
     */
    public static String getUserName() {
        return userNameList.get(random.nextInt(userNameList.size()));
    }

    /**
     * 获取指定索引位置的userName
     */
    public static String getUserName(int index) {
        return userNameList.get(index);
    }

    /**
     * 随机获取eventSource
     */
    public static String getEventSource() {
        return getRandomEnum(EventSource.class).name();
    }

    /**
     * 获取指定索引位置的eventSource
     */
    public static String getEventSource(int index) {
        return EventSource.values()[index].name();
    }

    /**
     * 随机获取eventLevel
     */
    public static String getEventLevel() {
        return getRandomEnum(EventLevel.class).name();
    }

    /**
     * 获取指定索引位置的eventLevel
     */
    public static String getEventLevel(int index) {
        return EventLevel.values()[index].name();
    }

    /**
     * 随机获取eventName
     */
    public static String getEventName() {
        return getRandomEnum(EventName.class).name();
    }

    /**
     * 获取指定索引位置的eventName
     */
    public static String getEventName(int index) {
        return EventName.values()[index].name();
    }

    /**
     * 随机获取eventResult
     */
    public static String getEventResult() {
        return getRandomEnum(EventResult.class).name();
    }

    /**
     * 获取指定索引位置的eventResult
     */
    public static String getEventResult(int index) {
        return EventResult.values()[index].name();
    }

    /**
     * 随机获取ipV4AddressList
     */
    public static String getIpV4Address(String userName) {
        List<String> ipV4AdressList = getUserNameAddressMap().get(userName);
        return ipV4AdressList.get(random.nextInt(ipV4AdressList.size()));
    }

    /**
     * 获取指定索引位置的ipV4AddressList
     */
    public static String getIpV4Address(String userName, int index) {
        List<String> ipV4AdressList = getUserNameAddressMap().get(userName);
        return ipV4AdressList.get(index);
    }

    /**
     * 随机获取location
     */
    public static String getLocation() {
        return getRandomEnum(EventLocation.class).name();
    }

    /**
     * 获取指定索引位置的location
     */
    public static String getLocation(int index) {
        return EventLocation.values()[index].name();
    }

    /**
     * 随机获取channel
     */
    public static String getChannel() {
        return getRandomEnum(EventChannel.class).name();
    }

    /**
     * 获取指定索引位置的channel
     */
    public static String getChannel(int index) {
        return EventChannel.values()[index].name();
    }

    /**
     * 随机获取userName与Address的map
     */
    public static Map<String, List<String>> getUserNameAddressMap() {
        Map<String, List<String>> userNameAddressMap;

        // 创建 ObjectMapper 实例
        ObjectMapper mapper = new ObjectMapper();

        // 使用ClassLoader获取资源文件的输入流
        try (InputStream is = MockEventUtils.class.getClassLoader().getResourceAsStream("mock/userNameAddress.json")) {
            if (is == null) {
                throw new RuntimeException("mock/userNameAddress.json not found");
            }
            // 读取 JSON 文件并转换成 Map<String, List<String>>
            userNameAddressMap = mapper.readValue(is, new TypeReference<Map<String, List<String>>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return userNameAddressMap;
    }

    /**
     * 生成随机递增的时间戳流
     * @param startMillis 开始的时间戳
     * @param durationMillis 时间跨度
     * @param perSecondCount 每秒生成的时间戳数量
     * @return 时间戳流
     */
    public static Stream<Long> generateTimeStampStream(long startMillis, long durationMillis, int perSecondCount) {
        long endMillis = startMillis + durationMillis;

        // 计算总的时间戳数量
        int totalMilliSeconds = (int)(durationMillis / 1000) * perSecondCount;
        if (durationMillis % 1000 != 0) {
            totalMilliSeconds += perSecondCount; // 如果有剩余的不完整秒，也按完整秒计算
        }

        return Stream.concat(
                Stream.of(startMillis, endMillis),
                Stream.generate(() -> startMillis + (long) (random.nextDouble() * (endMillis - startMillis)))
                        .limit(totalMilliSeconds - 2)
        ).sorted();
    }
}