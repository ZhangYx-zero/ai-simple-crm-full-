package com.aicrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟进记录 新增 参数。
 * customerId / opportunityId 至少填一个；两个都填时它们必须属于同一客户（Service 校验）。
 */
@Data
public class FollowUpDTO {

    private Long customerId;
    private Long opportunityId;

    /** 类型：电话 / 见面 / 微信 / 邮件（type=系统 由程序内部自动产生） */
    private String type;

    @NotBlank(message = "跟进内容不能为空")
    private String content;

    /** 下次跟进时间 */
    private LocalDateTime nextTime;
}
