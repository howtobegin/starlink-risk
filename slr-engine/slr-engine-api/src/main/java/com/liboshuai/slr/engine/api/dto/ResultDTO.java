package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.type.ResultDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(ResultDtoType.class)
public class ResultDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * kafka事件数据
     */
    private FlinkEventDTO flinkEventDTO;

    /**
     * 状态历史的记录数据
     */
    private StateHistoryDTO stateHistoryDTO;

    /**
     * 生成的预警信息
     */
    private AlertMessageDTO alertMessageDTO;
}
