package com.liboshuai.starlink.slr.engine.api.dto;

import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 事件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EventInfoDTO extends BaseDTO {
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
    /**
     * 事件属性组
     */
    private List<EventAttributeDTO> eventAttributeGroup;
}
