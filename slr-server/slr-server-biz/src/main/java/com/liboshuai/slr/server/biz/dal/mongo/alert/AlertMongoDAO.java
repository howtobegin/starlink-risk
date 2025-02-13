package com.liboshuai.slr.server.biz.dal.mongo.alert;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.server.biz.controller.alert.vo.AlertPageReqVO;
import com.liboshuai.slr.server.biz.dal.dataobject.alert.MongoAlertDO;
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
public class AlertMongoDAO {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 分页查询预警消息
     *
     * @return 分页结果
     */
    public PageResult<MongoAlertDO> selectPage(AlertPageReqVO alertPageReqVO) {
        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();

        String channel = alertPageReqVO.getChannel();
        if (channel != null && !channel.isEmpty()) {
            criteriaList.add(Criteria.where("channel").is(channel));
        }
        String ruleCode = alertPageReqVO.getRuleCode();
        if (ruleCode != null && !ruleCode.isEmpty()) {
            criteriaList.add(Criteria.where("ruleCode").is(ruleCode));
        }
        String message = alertPageReqVO.getMessage();
        if (message != null && !message.isEmpty()) {
            // 使用正则表达式进行模糊查询，'i'表示不区分大小写
            criteriaList.add(Criteria.where("message").regex(message, "i"));
        }
        LocalDateTime timeStart = alertPageReqVO.getTimeStart();
        LocalDateTime timeEnd = alertPageReqVO.getTimeEnd();
        if (timeStart != null && timeEnd != null) {
            criteriaList.add(Criteria.where("time").gte(timeStart).lte(timeEnd));
        } else if (timeStart != null) {
            criteriaList.add(Criteria.where("time").gte(timeStart));
        } else if (timeEnd != null) {
            criteriaList.add(Criteria.where("time").lte(timeEnd));
        }

        if (!criteriaList.isEmpty()) {
            Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            query.addCriteria(criteria);
        }

        int pageNo = alertPageReqVO.getPageNo();
        int pageSize = alertPageReqVO.getPageSize();
        // 设置分页参数
        // 页码从0开始，需要减1
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "time"));
        query.with(pageable);
        // 执行查询
        List<MongoAlertDO> list = mongoTemplate.find(query, MongoAlertDO.class);
        // 查询总记录数
        long total = mongoTemplate.count(query.skip(-1).limit(-1), MongoAlertDO.class);
        // 返回分页结果
        Page<MongoAlertDO> alertDOPage = new PageImpl<>(list, pageable, total);
        return new PageResult<>(
                alertDOPage.getContent(),
                alertDOPage.getTotalElements()
        );
    }
}
