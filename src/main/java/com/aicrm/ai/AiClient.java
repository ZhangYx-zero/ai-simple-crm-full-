package com.aicrm.ai;

/**
 * AI 客户端接口 —— 项目里所有大模型调用都走这里。
 * 通过 application.yml 的 ai.enabled 切换实现：
 *   ai.enabled=false  -> MockAiClient（写死回答，不用联网/装模型）
 *   ai.enabled=true   -> OpenAiCompatClient（HTTP 调本地 Ollama / DeepSeek / OpenAI 等）
 * 业务代码只依赖本接口，不关心底层是真是假 —— 这就是"面向接口编程"。
 */
public interface AiClient {

    /**
     * 发一次对话。
     *
     * @param systemPrompt 系统提示词（设定 AI 角色/约束，如"你是资深销售教练"）
     * @param userContent  用户内容（问题 + 我们拼好的客户上下文）
     * @return AI 回复文本
     */
    String chat(String systemPrompt, String userContent);

    /** 当前使用的模型名（用于落日志） */
    default String name() {
        return "unknown";
    }
}
