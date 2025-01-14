package com.liboshuai.slr.module.engine.framework.state;

import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapStateDescriptor;

/**
 * @Author: liboshuai
 * @Date: 2023-10-25 11:14
 **/
public class CommonStateDesc {

    /**
     * 规则广播流状态定义
     */
    public static MapStateDescriptor<Long, RuleInfoDTO> BROADCAST_RULE_MAP_STATE_DESC =
            new MapStateDescriptor<>("broadcastRuleMapState", Long.class, RuleInfoDTO.class);
    /**
     * 规则信息map状态定义
     */
    public static ListStateDescriptor<RuleInfoDTO> RULE_INFO_LIST_STATE_DESC =
            new ListStateDescriptor<>("ruleInfoListState", RuleInfoDTO.class);
    /**
     * 旧规则状态定义
     */
    public static MapStateDescriptor<Long, Void> OLD_RULE_MAP_STATE_DESC = new MapStateDescriptor<>("oldRuleMapState", Long.class, Void.class);

}
