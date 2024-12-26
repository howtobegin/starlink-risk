package com.liboshuai.slr.framework.common.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Schema(description = "基础 Response VO")
public class BaseRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2025-01-01")
    private LocalDateTime createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最后更新时间", example = "2025-01-01")
    private LocalDateTime updateTime;

    @Schema(description = "创建者", example = "boshuai.li")
    private String creator;

    @Schema(description = "更新者", example = "boshuai.li")
    private String updater;
}
