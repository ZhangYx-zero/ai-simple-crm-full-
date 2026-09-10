package com.aicrm.ai;

import com.aicrm.common.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容协议的真实 AI 客户端（JDK 自带 HttpClient，无第三方 SDK）。
 * 生效条件：ai.enabled=true。
 *
 * 兼容一切实现 OpenAI /chat/completions 协议的服务：
 *   - 本地 Ollama：       base-url=http://localhost:11434/v1，api-key 留空，model=qwen2.5
 *   - DeepSeek 等国内厂商：base-url=https://api.deepseek.com/v1，api-key=填你的 sk-xxx
 *   - OpenAI：            base-url=https://api.openai.com/v1，api-key=sk-xxx
 *
 * 请求/响应格式（OpenAI 兼容）：
 *   POST {base-url}/chat/completions
 *   body:  { model, messages:[{role:"system",content},{role:"user",content}] }
 *   resp:  choices[0].message.content
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "ai", name = "enabled", havingValue = "true")
public class OpenAiCompatClient implements AiClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.base-url:http://localhost:11434/v1}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:qwen2.5}")
    private String model;

    @Value("${ai.timeout-seconds:60}")
    private long timeoutSeconds;

    @Override
    public String chat(String systemPrompt, String userContent) {
        try {
            // 1) 拼 URL（去掉末尾多余的 /）
            String url = baseUrl.endsWith("/")
                    ? baseUrl.substring(0, baseUrl.length() - 1) + "/chat/completions"
                    : baseUrl + "/chat/completions";

            // 2) 拼请求体
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userContent))));

            // 3) 发 HTTP 请求
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(body, java.nio.charset.StandardCharsets.UTF_8));
            if (apiKey != null && !apiKey.isBlank()) {
                reqBuilder.header("Authorization", "Bearer " + apiKey);   // Ollama 不需要 key
            }

            HttpResponse<String> resp = httpClient.send(reqBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.error("[AI] HTTP {}：{}", resp.statusCode(), resp.body());
                throw new BusinessException("AI 服务返回异常（HTTP " + resp.statusCode() + "），请稍后再试");
            }

            // 4) 解析 choices[0].message.content
            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new BusinessException("AI 返回内容为空，请重试");
            }
            return content.asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // 连接超时 / 拒绝连接 / JSON 解析失败……统一转成友好提示，避免堆栈抛给前端
            log.error("[AI] 调用失败：{}", e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后再试（确认已启动 Ollama 或 AI 配置正确）");
        }
    }

    @Override
    public String name() {
        return model;
    }
}
