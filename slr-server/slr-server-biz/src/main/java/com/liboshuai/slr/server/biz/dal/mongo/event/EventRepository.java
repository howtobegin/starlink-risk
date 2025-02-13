package com.liboshuai.slr.server.biz.dal.mongo.event;

import com.liboshuai.slr.server.biz.dal.dataobject.event.MongoEventDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<MongoEventDO, String> {

    List<MongoEventDO> findAllByEventIdIn(List<String> eventIdList);

    void deleteByEventTimeBefore(long eventTime);
}
