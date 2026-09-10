package com.aicrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商机阶段推进参数。
 */
@Data
public class StageChangeDTO {

    /** 目标阶段 1~6 */
    @NotNull(message = "目标阶段不能为空")
    private Integer targetStage;

    /** 输单原因（targetStage=6 时建议填） */
    private String failReason;

    /** 赢单率覆盖值（targetStage=5 时可手动指定；不传则置 100） */
    private Integer winRate;
}
