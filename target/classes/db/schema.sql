-- ============================================================
-- AI-CRM 建表脚本（幂等：启动时自动执行，CREATE IF NOT EXISTS）
-- 说明：演示用户由 DataInitializer 在启动时自动插入（密码 BCrypt 运行时生成）
-- ============================================================

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密',
    `nickname`    VARCHAR(50),
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'SALES' COMMENT 'ADMIN/MANAGER/SALES',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_username` (`username`)
) COMMENT='用户';

CREATE TABLE IF NOT EXISTS `crm_lead` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(50)  NOT NULL COMMENT '线索名称',
    `company`     VARCHAR(100) COMMENT '公司',
    `phone`       VARCHAR(20),
    `source`      VARCHAR(20) COMMENT '来源:展会/网络/转介绍/外呼',
    `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0新 1跟进中 2已转客户 3已关闭',
    `owner_id`    BIGINT COMMENT '当前跟进销售',
    `remark`      VARCHAR(500),
    `deleted`     TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_owner` (`owner_id`)
) COMMENT='销售线索';

CREATE TABLE IF NOT EXISTS `crm_customer` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL,
    `industry`    VARCHAR(50) COMMENT '行业',
    `level`       VARCHAR(10)  COMMENT 'A/B/C',
    `source`      VARCHAR(20),
    `owner_id`    BIGINT COMMENT '归属销售(数据权限)',
    `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0潜在 1正式 2流失',
    `phone`       VARCHAR(20),
    `address`     VARCHAR(200),
    `remark`      VARCHAR(500),
    `deleted`     TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_owner` (`owner_id`)
) COMMENT='客户';

CREATE TABLE IF NOT EXISTS `crm_contact` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `customer_id` BIGINT NOT NULL,
    `name`        VARCHAR(50) NOT NULL,
    `position`    VARCHAR(50) COMMENT '职位',
    `phone`       VARCHAR(20),
    `wechat`      VARCHAR(50),
    `is_decision` TINYINT NOT NULL DEFAULT 0 COMMENT '是否决策人',
    `remark`      VARCHAR(500),
    `deleted`     TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_customer` (`customer_id`)
) COMMENT='联系人';

CREATE TABLE IF NOT EXISTS `crm_opportunity` (
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`         VARCHAR(100) NOT NULL COMMENT '商机名称',
    `customer_id`  BIGINT NOT NULL,
    `owner_id`     BIGINT COMMENT '跟进销售',
    `amount`       DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '预计金额(元)',
    `stage`        TINYINT NOT NULL DEFAULT 1 COMMENT '1初步接触 2需求确认 3方案报价 4商务谈判 5赢单 6输单',
    `expect_date`  DATE,
    `win_rate`     INT NOT NULL DEFAULT 10 COMMENT '赢单率%',
    `fail_reason`  VARCHAR(200) COMMENT '输单原因',
    `deleted`      TINYINT NOT NULL DEFAULT 0,
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_customer` (`customer_id`),
    KEY `idx_owner` (`owner_id`)
) COMMENT='商机';

CREATE TABLE IF NOT EXISTS `crm_follow_up` (
    `id`             BIGINT PRIMARY KEY AUTO_INCREMENT,
    `customer_id`    BIGINT COMMENT '关联客户',
    `opportunity_id` BIGINT COMMENT '关联商机',
    `type`           VARCHAR(20) COMMENT '电话/见面/微信/邮件/系统',
    `content`        VARCHAR(1000) COMMENT '跟进内容',
    `next_time`      DATETIME COMMENT '下次跟进',
    `create_by`      BIGINT COMMENT '记录人',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted`        TINYINT NOT NULL DEFAULT 0,
    KEY `idx_opportunity` (`opportunity_id`)
) COMMENT='跟进记录';

CREATE TABLE IF NOT EXISTS `crm_ai_chat_log` (
    `id`             BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`        BIGINT NOT NULL,
    `customer_id`    BIGINT COMMENT '上下文客户(可空)',
    `opportunity_id` BIGINT COMMENT '上下文商机(可空)',
    `question`       VARCHAR(1000) NOT NULL,
    `answer`         TEXT,
    `model`          VARCHAR(50),
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='AI助手问答日志';
