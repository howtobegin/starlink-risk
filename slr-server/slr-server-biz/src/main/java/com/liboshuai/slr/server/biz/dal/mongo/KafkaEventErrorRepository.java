package com.liboshuai.slr.server.biz.dal.mongo;

import com.liboshuai.slr.server.biz.dal.dataobject.kafkaEvent.KafkaEventErrorDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KafkaEventErrorRepository extends MongoRepository<KafkaEventErrorDO, String> {


}
