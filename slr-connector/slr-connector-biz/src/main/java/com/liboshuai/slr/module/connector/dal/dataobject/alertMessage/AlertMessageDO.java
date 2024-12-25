package com.liboshuai.slr.module.connector.dal.dataobject.alertMessage;

import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "slr_alert_message") // 指定集合名称
public class AlertMessageDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;

    /**
     * 规则编号
     */
    private String ruleCode;

    /**
     * 预警消息
     */
    private String alertMessage;

    /**
     * 预警时间
     * （格式：yyyy-MM-dd HH:mm:ss）
     */
    private String alertTime;

}
