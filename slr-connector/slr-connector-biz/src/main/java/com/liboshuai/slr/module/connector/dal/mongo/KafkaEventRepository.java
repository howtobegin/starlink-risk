package com.liboshuai.slr.module.connector.dal.mongo;

import com.liboshuai.slr.module.connector.dal.dataobject.kafkaEvent.KafkaEventDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KafkaEventRepository extends MongoRepository<KafkaEventDO, String> {

    List<KafkaEventDO> findAllByEventIdIn(List<Long> eventIdList);

    void deleteByEventTimeBefore(long eventTime);
}
