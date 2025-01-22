package com.liboshuai.slr.module.connector.service.kafkaEvent.impl;

import com.liboshuai.slr.module.connector.constants.AsyncExecutorConstants;
import com.liboshuai.slr.module.connector.convert.kafkaEvent.KafkaEventConvert;
import com.liboshuai.slr.module.connector.dal.mongo.KafkaEventRepository;
import com.liboshuai.slr.module.connector.service.kafkaEvent.MongoEventService;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoEventServiceImpl implements MongoEventService {

    private final KafkaEventRepository kafkaEventRepository;
    private final KafkaEventConvert kafkaEventConvert;

    @Override
    @Async(AsyncExecutorConstants.SAVE_EVENT_TO_MONGO_ASYNC_EXECUTOR)
    public void batchSaveEventToMongo(List<KafkaEventDTO> kafkaEventDTOList) {
        kafkaEventRepository.saveAll(kafkaEventConvert.batchConvertDto2Do(kafkaEventDTOList));
    }
}
