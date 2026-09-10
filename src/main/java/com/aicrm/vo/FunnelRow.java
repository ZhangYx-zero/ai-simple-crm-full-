package com.aicrm.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售漏斗一行：某个阶段有多少商机、金额合计多少。
 * 由 Mapper 手写 SQL 直接映射（total_amount 靠 map-underscore-to-camel-case 转成 totalAmount）。
 */
@Data
public class FunnelRow {

    /** 阶段 1~6（StageConstants） */
    private Integer stage;

    /** 商机数 */
    private Long cnt;

    /** 该阶段金额合计（元） */
    private BigDecimal totalAmount;

    /** 阶段中文名（Service 用 StageConstants.name() 填充，不在 SQL 里） */
    private String stageName;
}
