package com.aicrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客户归属转移参数（仅 ADMIN/MANAGER 可用）。
 */
@Data
public class TransferDTO {

    @NotNull(message = "新归属人不能为空")
    private Long newOwnerId;
}
