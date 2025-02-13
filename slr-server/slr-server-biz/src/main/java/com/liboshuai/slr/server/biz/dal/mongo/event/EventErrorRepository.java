package com.liboshuai.slr.server.biz.dal.mongo.event;

import com.liboshuai.slr.server.biz.dal.dataobject.event.MongoEventErrorDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventErrorRepository extends MongoRepository<MongoEventErrorDO, String> {


}
