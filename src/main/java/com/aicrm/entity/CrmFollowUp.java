package com.aicrm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟进记录 crm_follow_up。销售对客户/商机每次沟通动作的时间线。
 * type=系统 时表示由系统自动产生（如"商机阶段变更"），不是销售手动录入。
 */
@Data
@TableName("crm_follow_up")
public class CrmFollowUp {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联客户 id（可空，但 customerId 与 opportunityId 至少有一个） */
    private Long customerId;

    /** 关联商机 id（可空） */
    private Long opportunityId;

    /** 类型：电话 / 见面 / 微信 / 邮件 / 系统 */
    private String type;

    /** 跟进内容 */
    private String content;

    /** 下次跟进时间 */
    private LocalDateTime nextTime;

    /** 记录人 id */
    private Long createBy;

    /** 逻辑删除：0正常 1已删 */
    private Integer deleted;

    private LocalDateTime createTime;
}
