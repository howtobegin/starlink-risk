package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.baomidou.dynamic.datasource.annotation.Slave;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.KafkaEventDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.DorisEventMapper;
import com.liboshuai.slr.module.admin.service.riskRule.DorisEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DorisEventServiceImpl implements DorisEventService {

    @Resource
    private DorisEventMapper dorisEventMapper;

    @Slave
    @Override
    public void validateFlink() {
        // 查询所有满足条件的事件数据
        List<KafkaEventDO> kafkaEventDOList = dorisEventMapper.selectListByKey("GAME", "userId", "lottery");
        long count = 0;
        if (!CollectionUtils.isEmpty(kafkaEventDOList)) {
            count = kafkaEventDOList.size();
        }
        log.info("查询所有满足条件的事件数据数量: {}", count);

        // 按用户ID分组事件数据
        Map<String, List<KafkaEventDO>> userEventMap = kafkaEventDOList.stream()
                .collect(Collectors.groupingBy(KafkaEventDO::getTargetValue));

        // 遍历每个用户的事件数据
        for (Map.Entry<String, List<KafkaEventDO>> entry : userEventMap.entrySet()) {
            String userId = entry.getKey();
            List<KafkaEventDO> userEvents = entry.getValue();

            // 对用户事件按照事件时间排序
            userEvents.sort(Comparator.comparing(KafkaEventDO::getEventTime));

            // 记录上次预警的时间
            Date lastAlertTime = null;

            for (int i = 0; i < userEvents.size(); i++) {
                KafkaEventDO currentEvent = userEvents.get(i);
                Date currentTime = currentEvent.getEventTime();

                // 计算当前事件时间窗口内的累计事件值
                Date windowStartTime = new Date(currentTime.getTime() - 20 * 60 * 1000); // 20分钟窗口
                int cumulativeValue = 0;

                for (int j = i; j >= 0; j--) {
                    KafkaEventDO event = userEvents.get(j);
                    if (event.getEventTime().before(windowStartTime)) {
                        break;
                    }
                    cumulativeValue += Integer.parseInt(event.getEventValue());
                }

                // 判断是否超过阈值
                if (cumulativeValue > 10) {
                    // 判断预警间隔是否超过5分钟
                    if (lastAlertTime == null || (currentTime.getTime() - lastAlertTime.getTime()) >= 5 * 60 * 1000) {
                        // 生成预警信息
                        Map<String, String> attrMap = currentEvent.getEventAttrMap();
                        String bankName = attrMap.get("bankName");
                        String campaignName = attrMap.get("campaignName");
                        String campaignId = attrMap.get("campaignId");

                        String alertMessage = String.format("[异常高频抽奖]%s: 活动%s(%s)中游戏用户(%s)最近20分钟内抽奖数量为%d，超过10次，请您及时查看原因！",
                                bankName, campaignName, campaignId, userId, cumulativeValue);

                        // 输出预警信息（实际应用中可改为日志记录或发送通知）
                        System.out.println(alertMessage);

                        // 更新最后预警时间
                        lastAlertTime = currentTime;
                    }
                }
            }
        }
    }
}
