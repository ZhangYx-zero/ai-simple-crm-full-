package com.aicrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 线索 新增/编辑 参数。
 */
@Data
public class LeadDTO {

    @NotBlank(message = "线索名称不能为空")
    private String name;

    private String company;
    private String phone;
    private String source;   // 展会/网络/转介绍/外呼

    /** 0新 1跟进中 2已转客户 3已关闭（一般由"转客户"自动置 2） */
    private Integer status;

    private Long ownerId;
    private String remark;
}
