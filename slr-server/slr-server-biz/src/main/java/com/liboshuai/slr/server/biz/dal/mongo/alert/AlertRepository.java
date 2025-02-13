package com.liboshuai.slr.server.biz.dal.mongo.alert;

import com.liboshuai.slr.server.biz.dal.dataobject.alert.MongoAlertDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<MongoAlertDO, String> {

    List<MongoAlertDO> findByRuleCode(Long ruleCode);
}
