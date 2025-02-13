package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EventErrorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;

    /**
     * 错误级别
     */
    private String level;

    /**
     * 错误原因
     */
    private String cause;

    /**
     * 错误数据
     */
    private String data;

    /**
     * 错误时间
     */
    private LocalDateTime time;

}
