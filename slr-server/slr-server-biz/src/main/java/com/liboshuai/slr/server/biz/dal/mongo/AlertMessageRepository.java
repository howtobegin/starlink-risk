package com.liboshuai.slr.server.biz.dal.mongo;

import com.liboshuai.slr.server.biz.dal.dataobject.alertMessage.AlertMessageDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertMessageRepository extends MongoRepository<AlertMessageDO, String> {

    List<AlertMessageDO> findByRuleCode(Long ruleCode);
}
