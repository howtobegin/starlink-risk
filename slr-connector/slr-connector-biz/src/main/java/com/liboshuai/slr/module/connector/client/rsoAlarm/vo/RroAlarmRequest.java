package com.liboshuai.slr.module.connector.client.rsoAlarm.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RroAlarmRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 项目编号 HJF
     */
    private String projectNo;
    /**
     * 推送流水 唯一标识
     */
    private String pushSerialNo;
    /**
     * 预警等级
     */
    private String warningLevel = "4";
    /**
     * 推送的内容
     */
    private String alertMessage;
    /**
     * 预警类型
     */
    private String warningType = "RISK_BLOCK";
    /**
     * 预警时间 yyyyMMddHHmmss
     */
    private String warningTime;
    /**
     * 告警ip
     */
    private String warningIp;
}
