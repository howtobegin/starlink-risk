package com.liboshuai.slr.module.connector.dal.mongo;

import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.KafkaEventErrorDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KafkaEventErrorRepository extends MongoRepository<KafkaEventErrorDO, String> {


}
