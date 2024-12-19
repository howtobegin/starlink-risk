package com.liboshuai.starlink.slr.engine.test;

import org.apache.flink.api.common.state.MapStateDescriptor;

public class TestStateDescContainer {

    /**
     * 广播流状态定义
     */
    public static MapStateDescriptor<String, String> BROADCAST_MAP_STATE_DESC =
            new MapStateDescriptor<>("broadcastMapState", String.class, String.class);
}
