package com.liboshuai.slr.module.engine.common;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapStateDescriptor;

/**
 * @Author: liboshuai
 * @Date: 2023-10-25 11:14
 **/
public class StateDescContainer {

    /**
     * 规则广播流状态定义
     */
    public static MapStateDescriptor<String, RuleInfoDTO> BROADCAST_RULE_MAP_STATE_DESC =
            new MapStateDescriptor<>("broadcastRuleMapState", String.class, RuleInfoDTO.class);

    /**
     * 最近事件数据缓存状态定义
     */
    public static ListStateDescriptor<KafkaEventDTO> RECENT_EVENT_LIST_STATE_DESC = new ListStateDescriptor<>("recentEventCacheListState", KafkaEventDTO.class);

    /**
     * 旧规则状态定义
     */
    public static MapStateDescriptor<String, Object> OLD_RULE_MAP_STATE_DESC = new MapStateDescriptor<>("oldRuleMapState", String.class, Object.class);

}
