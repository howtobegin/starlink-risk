package com.liboshuai.slr.server.biz.service.kafkaEvent.impl;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.server.biz.constants.AsyncExecutorConstants;
import com.liboshuai.slr.server.biz.convert.event.EventConvert;
import com.liboshuai.slr.server.biz.dal.mongo.event.EventRepository;
import com.liboshuai.slr.server.biz.service.kafkaEvent.MongoEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoEventServiceImpl implements MongoEventService {

    private final EventRepository eventRepository;
    private final EventConvert eventConvert;

    @Override
    @Async(AsyncExecutorConstants.SAVE_EVENT_TO_MONGO_ASYNC_EXECUTOR)
    public void batchSaveEventToMongo(List<FlinkEventDTO> flinkEventDTOList) {
        eventRepository.saveAll(eventConvert.batchConvertDto2Do(flinkEventDTOList));
    }
}
