package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.type.FlinkResultDtoType;
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
@TypeInfo(FlinkResultDtoType.class)
public class FlinkResultDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * Flink事件数据
     */
    private FlinkEventDTO flinkEventDTO;

    /**
     * 规则状态记录，用于运算机下线清理状态
     */
    private StateDTO stateDTO;

    /**
     * 生成的预警信息
     */
    private AlertDTO alertDTO;
}
