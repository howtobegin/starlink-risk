package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.module.engine.type.ResultDtoType;
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

    private KafkaEventDTO kafkaEventDTO;

    private RuleKeyHistoryDTO ruleKeyHistoryDTO;

    private AlertMessageDTO alertMessageDTO;
}
