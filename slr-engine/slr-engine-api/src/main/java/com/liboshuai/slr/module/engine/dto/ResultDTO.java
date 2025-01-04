package com.liboshuai.slr.module.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResultDTO implements Serializable {

    private static final long serialVersionUID = 1;

    private RuleKeyHistoryDTO ruleKeyHistoryDTO;

    private AlertMessageDTO alertMessageDTO;
}
