package com.aicrm.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 模拟 AI 客户端。
 * 生效条件：ai.enabled=false 或【未配置】（matchIfMissing=true）——即默认就能用，零依赖。
 * 用于：不想装 Ollama / 断网演示 / 接口联调时，先跑通全流程。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "ai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockAiClient implements AiClient {

    @Override
    public String chat(String systemPrompt, String userContent) {
        log.info("[MockAi] 收到问题：{}", userContent);
        // 写死一段"销售教练"式回答，结构尽量贴近真实大模型
        return """
                【当前是模拟回答：未启用真实 AI】
                要启用真实大模型，请把 application.yml 的 ai.enabled 设为 true，
                并确保本机已装 Ollama 且拉取了 qwen2.5 模型（ollama pull qwen2.5）。

                —— 销售建议 ——
                1. 先明确客户当前最关心的业务痛点，用提问代替推销；
                2. 结合系统里的客户与跟进上下文，找到上次沟通的遗留事项，主动推进；
                3. 商机阶段每前进一步，都要同步更新跟进记录，保持信息可追溯。
                """.trim();
    }

    @Override
    public String name() {
        return "mock";
    }
}
