package com.liboshuai.slr.module.connector.dao.mongo;

import com.liboshuai.slr.module.connector.pojo.mongo.AlertMessageMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertMessageRepository extends MongoRepository<AlertMessageMongo, String> {
}
