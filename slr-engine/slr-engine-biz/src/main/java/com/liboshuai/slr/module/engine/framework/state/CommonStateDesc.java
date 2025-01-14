package com.liboshuai.slr.module.engine.framework.state;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import org.apache.flink.api.common.state.MapStateDescriptor;

/**
 * @Author: liboshuai
 * @Date: 2023-10-25 11:14
 **/
public class CommonStateDesc {

    /**
     * 规则广播流状态定义
     */
    public static MapStateDescriptor<String, RuleInfoDTO> BROADCAST_RULE_MAP_STATE_DESC =
            new MapStateDescriptor<>("broadcastRuleMapState", String.class, RuleInfoDTO.class);

    /**
     * 最近事件数据缓存状态定义
     */
    public static MapStateDescriptor<KafkaEventDTO, Object> RECENT_EVENT_MAP_STATE_DESC = new MapStateDescriptor<>("recentEventCacheMapState", KafkaEventDTO.class, Object.class);

    /**
     * 旧规则状态定义
     */
    public static MapStateDescriptor<String, Void> OLD_RULE_MAP_STATE_DESC = new MapStateDescriptor<>("oldRuleMapState", String.class, Void.class);

}
