package com.aicrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客户 新增/编辑 参数。
 * ownerId：为空时默认归当前登录人；SALES 新建时强制归自己（Service 里处理）。
 */
@Data
public class CustomerDTO {

    @NotBlank(message = "客户名称不能为空")
    private String name;

    private String industry;
    private String level;      // A/B/C
    private String source;
    private Long ownerId;      // 归属销售（管理员可指定）
    private Integer status;    // 0潜在 1正式 2流失
    private String phone;
    private String address;
    private String remark;
}
