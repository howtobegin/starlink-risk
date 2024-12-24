package com.liboshuai.starlink.slr.connector.pojo.entity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.starlink.slr.framework.mybatis.core.dataobject.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 事件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_event")
@EqualsAndHashCode(callSuper = true)
public class RuleEventEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
    /**
     * 事件编号
     */
    private String eventCode;
    /**
     * 事件名称
     */
    private String eventName;
    /**
     * 事件描述
     */
    private String eventDesc;
}
