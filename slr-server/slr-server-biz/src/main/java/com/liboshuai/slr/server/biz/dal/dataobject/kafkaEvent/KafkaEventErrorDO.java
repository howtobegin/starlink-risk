package com.liboshuai.slr.server.biz.dal.dataobject.kafkaEvent;

import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "slr_kafka_event_error") // 指定集合名称
public class KafkaEventErrorDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
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
