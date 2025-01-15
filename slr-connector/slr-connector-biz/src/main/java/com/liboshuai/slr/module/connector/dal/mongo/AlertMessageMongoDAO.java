package com.liboshuai.slr.module.connector.dal.mongo;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqApiDTO;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AlertMessageMongoDAO {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 分页查询预警消息
     *
     * @return 分页结果
     */
    public PageResult<AlertMessageDO> selectPage(AlertMessagePageReqApiDTO alertMessagePageReqApiDTO) {
        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();

        String channel = alertMessagePageReqApiDTO.getChannel();
        if (channel != null && !channel.isEmpty()) {
            criteriaList.add(Criteria.where("channel").is(channel));
        }
        String ruleCode = alertMessagePageReqApiDTO.getRuleCode();
        if (ruleCode != null && !ruleCode.isEmpty()) {
            criteriaList.add(Criteria.where("ruleCode").is(ruleCode));
        }
        String alertMessage = alertMessagePageReqApiDTO.getAlertMessage();
        if (alertMessage != null && !alertMessage.isEmpty()) {
            // 使用正则表达式进行模糊查询，'i'表示不区分大小写
            criteriaList.add(Criteria.where("alertMessage").regex(alertMessage, "i"));
        }
        LocalDateTime alertTimeStart = alertMessagePageReqApiDTO.getAlertTimeStart();
        LocalDateTime alertTimeEnd = alertMessagePageReqApiDTO.getAlertTimeEnd();
        if (alertTimeStart != null && alertTimeEnd != null) {
            criteriaList.add(Criteria.where("alertTime").gte(alertTimeStart).lte(alertTimeEnd));
        } else if (alertTimeStart != null) {
            criteriaList.add(Criteria.where("alertTime").gte(alertTimeStart));
        } else if (alertTimeEnd != null) {
            criteriaList.add(Criteria.where("alertTime").lte(alertTimeEnd));
        }

        if (!criteriaList.isEmpty()) {
            Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            query.addCriteria(criteria);
        }

        int pageNo = alertMessagePageReqApiDTO.getPageNo();
        int pageSize = alertMessagePageReqApiDTO.getPageSize();
        // 设置分页参数
        // 页码从0开始，需要减1
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "alertTime"));
        query.with(pageable);
        // 执行查询
        List<AlertMessageDO> list = mongoTemplate.find(query, AlertMessageDO.class);
        // 查询总记录数
        long total = mongoTemplate.count(query.skip(-1).limit(-1), AlertMessageDO.class);
        // 返回分页结果
        Page<AlertMessageDO> alertMessageDOPage = new PageImpl<>(list, pageable, total);
        return new PageResult<>(
                alertMessageDOPage.getContent(),
                alertMessageDOPage.getTotalElements()
        );
    }
}
