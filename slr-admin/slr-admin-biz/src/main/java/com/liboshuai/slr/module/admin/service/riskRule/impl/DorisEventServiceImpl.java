package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.baomidou.dynamic.datasource.annotation.Slave;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.DorisEventDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.DorisEventMapper;
import com.liboshuai.slr.module.admin.service.riskRule.DorisEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
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
        List<DorisEventDO> dorisEventDOList = dorisEventMapper.selectListByKey("GAME", "userId", "lottery");
        long count = 0;
        if (!CollectionUtils.isEmpty(dorisEventDOList)) {
            count = dorisEventDOList.size();
        }
        log.info("查询所有满足条件的事件数据数量: {}", count);
        if (!dorisEventDOList.isEmpty()) {
            DorisEventDO dorisEventDO = dorisEventDOList.get(0);
            log.info("dorisEventDO: {}", dorisEventDO);
        }

        // 按用户ID分组事件数据
        Map<String, List<DorisEventDO>> userEventMap = dorisEventDOList.stream()
                .collect(Collectors.groupingBy(DorisEventDO::getTargetValue));

        long alertCount = 0;

        // 遍历每个用户的事件数据
        for (Map.Entry<String, List<DorisEventDO>> entry : userEventMap.entrySet()) {
            String userId = entry.getKey();
            List<DorisEventDO> userEvents = entry.getValue();

            // 对用户事件按照事件时间排序
            userEvents.sort(Comparator.comparing(DorisEventDO::getEventTime));

            // 记录上次预警的时间
            LocalDateTime lastAlertTime = null;


            for (int i = 0; i < userEvents.size(); i++) {
                DorisEventDO currentEvent = userEvents.get(i);
                LocalDateTime currentTime = currentEvent.getEventTime();

                // 计算当前事件时间窗口内的累计事件值
                LocalDateTime windowStartTime = currentTime.minusMinutes(20); // 20分钟窗口
                int cumulativeValue = 0;

                for (int j = i; j >= 0; j--) {
                    DorisEventDO event = userEvents.get(j);
                    if (event.getEventTime().isBefore(windowStartTime)) {
                        break;
                    }
                    cumulativeValue += Integer.parseInt(event.getEventValue());
                }

                // 判断是否超过阈值
                if (cumulativeValue > 10) {
                    // 判断预警间隔是否超过5分钟
                    if (lastAlertTime == null || Duration.between(lastAlertTime, currentTime).toMinutes() >= 5) {
                        // 生成预警信息
                        String eventAttrMap = currentEvent.getEventAttrMap();
                        Map<String, String> attrMap = JsonUtils.parseObject(eventAttrMap, Map.class);
                        String bankName = attrMap.get("bankName");
                        String campaignName = attrMap.get("campaignName");
                        String campaignId = attrMap.get("campaignId");

                        String alertMessage = String.format("[异常高频抽奖]%s: 活动%s(%s)中游戏用户(%s)最近20分钟内抽奖数量为%d，超过10次，请您及时查看原因！",
                                bankName, campaignName, campaignId, userId, cumulativeValue);

                        // 输出预警信息（实际应用中可改为日志记录或发送通知）
                        log.info(alertMessage);
                        alertCount++;

                        // 更新最后预警时间
                        lastAlertTime = currentTime;
                    }
                }
            }
        }
        log.info("预警条数：{}", alertCount);
    }
}

