package com.aicrm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 助手问答日志 crm_ai_chat_log。
 * 注意：这张表【没有 deleted 列】，所以实体也【不写 deleted 字段】——
 * 否则全局逻辑删除（logic-delete-field: deleted）会往 SQL 里拼 deleted=0 导致报错。
 */
@Data
@TableName("crm_ai_chat_log")
public class CrmAiChatLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提问人 id */
    private Long userId;

    /** 上下文客户（可空） */
    private Long customerId;

    /** 上下文商机（可空） */
    private Long opportunityId;

    private String question;
    private String answer;

    /** 实际使用的大模型名（方便看日志） */
    private String model;

    private LocalDateTime createTime;
}
