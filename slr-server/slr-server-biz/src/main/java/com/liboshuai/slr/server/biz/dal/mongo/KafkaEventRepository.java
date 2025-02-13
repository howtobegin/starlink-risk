package com.liboshuai.slr.server.biz.dal.mongo;

import com.liboshuai.slr.server.biz.dal.dataobject.kafkaEvent.KafkaEventDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KafkaEventRepository extends MongoRepository<KafkaEventDO, String> {

    List<KafkaEventDO> findAllByEventIdIn(List<String> eventIdList);

    void deleteByEventTimeBefore(long eventTime);
}
