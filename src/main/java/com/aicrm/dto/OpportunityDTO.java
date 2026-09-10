package com.aicrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商机 新增/编辑 参数。
 */
@Data
public class OpportunityDTO {

    @NotBlank(message = "商机名称不能为空")
    private String name;

    @NotNull(message = "所属客户不能为空")
    private Long customerId;

    private Long ownerId;

    /** 预计金额（元） */
    private BigDecimal amount;

    /** 阶段 1~6（默认 1 初步接触，Service 里补） */
    private Integer stage;

    private LocalDate expectDate;

    /** 赢单率 %（默认 10） */
    private Integer winRate;
}
