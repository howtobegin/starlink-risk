package com.liboshuai.slr.engine.biz.framework.state;

import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;

/**
 * @Author: liboshuai
 * @Date: 2023-10-25 11:14
 **/
public class CommonStateDesc {

    /**
     * 规则广播流状态定义
     */
    public static MapStateDescriptor<Long, RuleInfoDTO> BROADCAST_RULE_MAP_STATE_DESC =
            new MapStateDescriptor<>("broadcastRuleMapState", Types.LONG, Types.POJO(RuleInfoDTO.class));
    /**
     * 规则信息map状态定义
     */
    public static ListStateDescriptor<RuleInfoDTO> RESTORE_RULE_INFO_LIST_STATE_DESC =
            new ListStateDescriptor<>("restoreRuleInfoListState", Types.POJO(RuleInfoDTO.class));
    /**
     * 旧规则状态定义
     */
    public static MapStateDescriptor<Long, Void> OLD_RULE_MAP_STATE_DESC = new MapStateDescriptor<>("oldRuleMapState", Types.LONG, Types.VOID);

}
