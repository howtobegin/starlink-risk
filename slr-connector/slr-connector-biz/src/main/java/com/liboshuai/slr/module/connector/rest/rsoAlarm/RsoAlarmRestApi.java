package com.liboshuai.slr.module.connector.rest.rsoAlarm;

import com.liboshuai.slr.framework.common.util.date.DateUtils;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.connector.rest.rsoAlarm.vo.RroAlarmRequest;
import com.liboshuai.slr.module.connector.rest.rsoAlarm.vo.RsoAlarmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsoAlarmRestApi {

    private static final int ALERT_MESSAGE_MAX_LENGTH = 300;
    private final RestTemplate restTemplate;
    @Value("${slr-connector.rso.alert-address}")
    private String rsoAlertAddress;

    /**
     * 发送消息到荣数运营平台
     * 预警类型默认业务预警
     * 预警级别默认二级预警: 告警方式为(电话+微信)
     *
     * @param projectNo    在荣数运营中配置的项目编号
     * @param warningLevel 预警等级
     * @param alertMessage 需要告警的消息 (最大300个字节,超过300部分进行截取)
     * @return RSOAlarmMessageResponse 发送响应结果
     */
    public RsoAlarmResponse sendMsgToRso(String projectNo, String warningLevel, String waningTime, String alertMessage) {
        if (StringUtils.isBlank(alertMessage) || StringUtils.isBlank(projectNo) || StringUtils.isBlank(rsoAlertAddress)) {
            log.error("发送参数为空，请检查项目编号、请求地址或告警信息是否为空。");
            return null;
        }

        // 按长度限制处理告警消息
        if (alertMessage.length() > ALERT_MESSAGE_MAX_LENGTH) {
            alertMessage = alertMessage.substring(0, ALERT_MESSAGE_MAX_LENGTH);
        }

        try {
            RroAlarmRequest alarmMessageDTO = createAlertMessageRequest(projectNo, warningLevel, waningTime, alertMessage);

            // 使用 RestTemplate 发起 POST 请求并处理响应
            String responseJson = sendPostForJson(rsoAlertAddress, alarmMessageDTO);

            return StringUtils.isNotBlank(responseJson)
                    ? JsonUtils.parseObject(responseJson, RsoAlarmResponse.class)
                    : null;
        } catch (Exception e) {
            log.error("发送消息到荣数运营平台失败。", e);
            return null;
        }
    }

    /**
     * 使用 RestTemplate 进行 POST 请求，发送 JSON 数据
     *
     * @param url        请求地址
     * @param requestObj 请求对象，将被转换为 JSON
     * @return 响应 JSON 字符串
     */
    private String sendPostForJson(String url, Object requestObj) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf8");

            // 构造请求实体
            HttpEntity<String> requestEntity = new HttpEntity<>(JsonUtils.toJsonString(requestObj), headers);

            // 发起 POST 请求
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("请求地址 {} 失败，状态码为：{}", url, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("RestTemplate POST 请求发生异常。", e);
        }

        return null; // 请求失败或无响应内容
    }

    /**
     * 构造警告消息请求体
     *
     * @param projectNo    项目编号
     * @param alertMessage 警告消息内容
     * @return 构造的 RSOAlarmMessageRequest 对象
     */
    private RroAlarmRequest createAlertMessageRequest(String projectNo, String warningLevel, String waningTime, String alertMessage) {
        RroAlarmRequest alarmMessageDTO = new RroAlarmRequest();
        String dateTime = getFormatDate(new Date());

        alarmMessageDTO.setProjectNo(projectNo);
        alarmMessageDTO.setWarningLevel(warningLevel);
        alarmMessageDTO.setAlertMessage(alertMessage);
        alarmMessageDTO.setWarningTime(waningTime);
        alarmMessageDTO.setPushSerialNo(dateTime + getRandomNum());
        alarmMessageDTO.setWarningIp(getLocalIp());

        return alarmMessageDTO;
    }

    /**
     * 获取随机的数字字符串
     *
     * @return 随机数字字符串
     */
    private String getRandomNum() {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            val.append(random.nextInt(10)); // 不需要将 int 转换为 String
        }
        return val.toString();
    }

    /**
     * 获取本机 IP 地址
     *
     * @return 当前机器的 IP 地址
     */
    private String getLocalIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            log.error("获取本机 IP 地址失败。", e);
            return null; // 返回 null 以表示无法获取 IP
        }
    }

    /**
     * 输出字符串类型的格式化日期
     *
     * @param dt Date
     * @return 格式化后的日期字符串
     */
    private String getFormatDate(Date dt) {
        if (dt == null) {
            return "";
        }

        SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_COMPACT);
        return formatter.format(dt);
    }
}