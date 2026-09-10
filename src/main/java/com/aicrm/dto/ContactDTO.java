package com.aicrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 联系人 新增 参数。所属客户由 URL 上的 {customerId} 决定，不走 body。
 */
@Data
public class ContactDTO {

    @NotBlank(message = "联系人姓名不能为空")
    private String name;

    private String position;
    private String phone;
    private String wechat;

    /** 是否决策人：0否 1是 */
    private Integer isDecision;

    private String remark;
}
