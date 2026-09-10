package com.aicrm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户 crm_customer。CRM 的核心主数据。
 * owner_id 是数据权限关键字段：SALES 只能看/改自己名下客户；MANAGER/ADMIN 看全部。
 */
@Data
@TableName("crm_customer")
public class CrmCustomer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户名称（公司） */
    private String name;

    /** 行业 */
    private String industry;

    /** 等级：A / B / C（A 最高优） */
    private String level;

    /** 来源 */
    private String source;

    /** 归属销售（数据权限字段） */
    private Long ownerId;

    /** 状态：0潜在 1正式 2流失 */
    private Integer status;

    private String phone;
    private String address;
    private String remark;

    /** 逻辑删除：0正常 1已删 */
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
