package com.liboshuai.starlink.slr.connector.pojo.vo.kafkaEvent;

import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 业务平台上送事件数据到 kafka 入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KafkaEventGroupReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
    /**
     * kafka事件组（元素个数限制为10个）
     */
    private List<KafkaEventReqVO> kafkaEventReqVOList;
}
