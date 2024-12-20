package com.liboshuai.starlink.slr.engine.dto;

import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 预警信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class WarnInfoDTO implements Serializable {

    private static final long serialVersionUID = -3125924174631531244L;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;

    /**
     * key编号
     */
    private String keyCode;

    /**
     * key名称
     */
    private String keyValue;

    /**
     * 属性
     */
    private Map<String, Object> attribute;

}
