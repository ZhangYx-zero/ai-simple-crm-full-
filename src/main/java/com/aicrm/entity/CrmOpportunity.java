package com.aicrm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商机 crm_opportunity。一次"可能成交的生意"，挂在客户下。
 * stage 走状态机：1初步接触→2需求确认→3方案报价→4商务谈判→5赢单/6输单（终态，不可回退）。
 */
@Data
@TableName("crm_opportunity")
public class CrmOpportunity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商机名称 */
    private String name;

    /** 所属客户 id */
    private Long customerId;

    /** 跟进销售（数据权限字段） */
    private Long ownerId;

    /** 预计金额（元）。金额一律用 BigDecimal，避免 Double 精度问题 */
    private BigDecimal amount;

    /** 阶段：1~6（见 StageConstants） */
    private Integer stage;

    /** 预计成交日期 */
    private LocalDate expectDate;

    /** 赢单率 %（默认 10，赢单后置 100） */
    private Integer winRate;

    /** 输单原因（仅 stage=6 时记录） */
    private String failReason;

    /** 逻辑删除：0正常 1已删 */
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
