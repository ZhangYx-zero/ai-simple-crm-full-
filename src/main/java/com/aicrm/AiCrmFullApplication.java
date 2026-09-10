package com.aicrm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 智能销售 CRM（完整版）启动类。
 * 注意：为了让 ai-crm / ai-crm-full 两个项目能同时跑在 8080，
 *       可给其中一个改端口（见 application.yml 里 server.port）。
 */
@SpringBootApplication
@MapperScan("com.aicrm.mapper")   // 扫描 Mapper 接口，生成代理
public class AiCrmFullApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCrmFullApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  AI-CRM-FULL 启动成功！接口文档：http://localhost:8081/doc.html");
        System.out.println("  演示账号：admin/123456  manager/123456  seller/123456");
        System.out.println("  AI：默认连本地 Ollama（http://localhost:11434，模型 qwen2.5）");
        System.out.println("       未装 Ollama 时可把 application.yml 的 ai.enabled 设为 false");
        System.out.println("==========================================================");
    }
}
