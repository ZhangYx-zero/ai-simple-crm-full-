package com.aicrm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人 crm_contact。挂在某个客户下，一个客户可有多位联系人。
 * is_decision 表示是否"决策人"，谈单时优先找决策人。
 */
@Data
@TableName("crm_contact")
public class CrmContact {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属客户 id */
    private Long customerId;

    private String name;

    /** 职位 */
    private String position;

    private String phone;
    private String wechat;

    /** 是否决策人：0否 1是 */
    private Integer isDecision;

    private String remark;

    /** 逻辑删除：0正常 1已删 */
    private Integer deleted;

    private LocalDateTime createTime;
}
