package com.aicrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 助手提问参数。
 * 可带 customerId / opportunityId，AI 会把客户档案+商机+跟进记录作为上下文来回答。
 */
@Data
public class AiChatDTO {

    @NotBlank(message = "问题不能为空")
    private String question;

    private Long customerId;
    private Long opportunityId;
}
