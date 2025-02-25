package com.liboshuai.slr.server.biz.convert.rule;

import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.TimeRangeSaveReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.TimeRangeRespVO;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleCondTimeRangeDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimeRangeConvert {

    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "string2LocalTime")
    @Mapping(source = "endTime", target = "endTime", qualifiedByName = "string2LocalTime")
    @Mapping(source = "daysOfWeek", target = "daysOfWeek", qualifiedByName = "list2String")
    List<RuleCondTimeRangeDO> batchConvertRepVo2Do(List<TimeRangeSaveReqVO> timeRangeSaveReqVOList);

    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "localTime2String")
    @Mapping(source = "endTime", target = "endTime", qualifiedByName = "localTime2String")
    @Mapping(source = "daysOfWeek", target = "daysOfWeek", qualifiedByName = "string2List")
    List<TimeRangeRespVO> batchConvertDo2RespVo(List<RuleCondTimeRangeDO> timeRangeDOList);

    default LocalTime string2LocalTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        return LocalDateTimeUtils.convertString2LocalTime(time);
    }

    default String localTime2String(LocalTime time) {
        if (Objects.isNull(time)) {
            return null;
        }
        return LocalDateTimeUtils.convertLocalTime2String(time);
    }

    default List<String> string2List(String daysOfWeek) {
        if (!StringUtils.hasText(daysOfWeek)) {
            return null;
        }
        return Arrays.asList(daysOfWeek.split(DefaultConstants.COMMA));
    }

    default String list2String(List<String> daysOfWeek) {
        if (Objects.isNull(daysOfWeek)) {
            return null;
        }
        return String.join(DefaultConstants.COMMA, daysOfWeek);
    }
}