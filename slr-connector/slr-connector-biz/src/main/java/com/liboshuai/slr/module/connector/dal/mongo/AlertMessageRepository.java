package com.liboshuai.slr.module.connector.dal.mongo;

import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertMessageRepository extends MongoRepository<AlertMessageDO, String> {
}
