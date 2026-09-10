package com.aicrm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 销售线索 crm_lead。
 * 线索是"还没成为客户"的潜在销售机会来源（展会/网络/转介绍…），
 * 可一步步跟进，最终通过"转客户"生成正式客户 + 联系人。
 */
@Data
@TableName("crm_lead")
public class CrmLead {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 线索名称（通常是人名或公司名） */
    private String name;

    /** 公司（转客户时优先用它当客户名） */
    private String company;

    private String phone;

    /** 来源：展会 / 网络 / 转介绍 / 外呼 */
    private String source;

    /** 状态：0新 1跟进中 2已转客户 3已关闭 */
    private Integer status;

    /** 当前跟进销售（数据权限字段） */
    private Long ownerId;

    private String remark;

    /** 逻辑删除：0正常 1已删（全局 logic-delete-field 自动处理，实体里必须有这个字段） */
    private Integer deleted;

    /** 以下两个时间由数据库 DEFAULT CURRENT_TIMESTAMP 自动填充，Java 端不赋值 */
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
