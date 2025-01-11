package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.baomidou.dynamic.datasource.annotation.Slave;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.DorisEventDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.DorisEventMapper;
import com.liboshuai.slr.module.admin.service.riskRule.DorisEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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
        List<DorisEventDO> dorisEventDOList = dorisEventMapper.selectListByKey("GAME", "userId", "lottery");
        if (CollectionUtils.isEmpty(dorisEventDOList)) {
            log.warn("没有符合规则条件的历史事件数据！");
            return;
        }
        log.info("查询到符合规则条件的历史事件数据数量：{}", dorisEventDOList.size());

        // 根据targetValue进行分组
        Map<String, List<DorisEventDO>> targetValueAndDorisEventDOMap = dorisEventDOList.stream()
                .collect(Collectors.groupingBy(DorisEventDO::getTargetValue));
        // 遍历每个targetValue下的数据，进行风控规则判断
        long alertCount = 0L;
        for (Map.Entry<String, List<DorisEventDO>> entry : targetValueAndDorisEventDOMap.entrySet()) {
            String targetValue = entry.getKey();
            List<DorisEventDO> dorisEventDOS = entry.getValue();
            // 最近一次预警时间（针对当前 TARGET_VALUE）
            long lastAlertTimestamp = 0L;
            if (CollectionUtils.isEmpty(dorisEventDOS)) {
                continue;
            }
            // 获取第一个和最后一个事件的时间戳
            DorisEventDO firstDorisEventDO = dorisEventDOS.get(0);
            long firstEventTimestamp = LocalDateTimeUtils.convertLocalDateTime2Timestamp(firstDorisEventDO.getEventTime());
            DorisEventDO latestDorisEventDO = dorisEventDOS.get(dorisEventDOS.size() - 1);
            long latestEventTimestamp = LocalDateTimeUtils.convertLocalDateTime2Timestamp(latestDorisEventDO.getEventTime());

            // 定义窗口大小和步长（以毫秒为单位）
            long windowSize = 20 * 60 * 1000; // 20分钟
            long step = 60 * 1000;            // 1分钟

            // 计算窗口的起始时间
            long earliestWindowStartTimeStamp = firstEventTimestamp - windowSize + step;
            earliestWindowStartTimeStamp = earliestWindowStartTimeStamp - (earliestWindowStartTimeStamp % step); // 对齐到整分钟
            long latestWindowStartTimeStamp = latestEventTimestamp - (latestEventTimestamp % step);

            for (long windowStartTimeStamp = earliestWindowStartTimeStamp; windowStartTimeStamp <= latestWindowStartTimeStamp; windowStartTimeStamp += step) {
                long windowEndTimeStamp = windowStartTimeStamp + windowSize;

                // 过滤出在当前窗口内的事件
                long finalWindowStartTimeStamp = windowStartTimeStamp;
                List<DorisEventDO> windowsDorisEventDOList = dorisEventDOS.stream()
                        .filter(eventDO -> {
                            long eventTimestamp = LocalDateTimeUtils.convertLocalDateTime2Timestamp(eventDO.getEventTime());
                            return eventTimestamp >= finalWindowStartTimeStamp && eventTimestamp < windowEndTimeStamp;
                        })
                        .collect(Collectors.toList());

                if (CollectionUtils.isEmpty(windowsDorisEventDOList)) {
                    continue;
                }

                // 计算事件值累计和
                long eventValueSum = windowsDorisEventDOList.stream()
                        .mapToLong(eventDO -> Long.parseLong(eventDO.getEventValue()))
                        .sum();

                // 增加详细日志
                log.debug("TARGET_VALUE: {}, 窗口 [{} - {})，事件数量：{}，事件值累计和：{}",
                        targetValue,
                        LocalDateTimeUtils.convertTimestamp2String(windowStartTimeStamp),
                        LocalDateTimeUtils.convertTimestamp2String(windowEndTimeStamp),
                        windowsDorisEventDOList.size(),
                        eventValueSum);

                if (eventValueSum > 10 && (windowEndTimeStamp - lastAlertTimestamp >= 5 * 60 * 1000)) {
                    DorisEventDO latestDorisEventDo = windowsDorisEventDOList.get(windowsDorisEventDOList.size() - 1);
                    Map<String, String> map = JsonUtils.parseObject(latestDorisEventDo.getEventAttrMap(), Map.class);
                    String alertMessage = String.format("[异常高频抽奖]%s: 活动%s(%s)中游戏用户(%s)最近20分钟内抽奖数量为%d，超过10次，请您及时查看原因！",
                            map.get("bankName"), map.get("campaignName"), map.get("campaignId"), targetValue, eventValueSum);
                    log.info(LocalDateTimeUtils.convertTimestamp2String(windowEndTimeStamp) + ": " + alertMessage);
                    alertCount++;

                    // 更新 lastAlertTimestamp 为当前窗口的结束时间
                    lastAlertTimestamp = windowEndTimeStamp;
                }
            }
        }
        log.info("触发的预警信息条数: {}", alertCount);
    }
}



