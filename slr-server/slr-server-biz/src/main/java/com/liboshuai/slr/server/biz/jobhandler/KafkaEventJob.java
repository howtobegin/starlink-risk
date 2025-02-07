package com.liboshuai.slr.server.biz.jobhandler;

import com.liboshuai.slr.framework.takeTime.core.aop.TakeTime;
import com.liboshuai.slr.server.biz.service.kafkaEvent.KafkaEventService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventJob {
    private final KafkaEventService kafkaEventService;

    /**
     * 删除mongo中过期事件数据
     */
    @TakeTime
    @XxlJob("deleteOldEventFromMongo")
    public void deleteOldEventFromMongo() throws Exception {
        kafkaEventService.deleteOldEventFromMongo();
    }
}
