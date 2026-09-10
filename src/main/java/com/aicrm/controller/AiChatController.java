package com.aicrm.controller;

import com.aicrm.common.Result;
import com.aicrm.dto.AiChatDTO;
import com.aicrm.entity.CrmAiChatLog;
import com.aicrm.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 助手接口。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /** 向 AI 提问（可携带客户/商机上下文） */
    @PostMapping("/chat")
    public Result<String> chat(@Valid @RequestBody AiChatDTO dto) {
        return Result.ok(aiChatService.chat(dto));
    }

    /** 我的提问历史 */
    @GetMapping("/chats")
    public Result<List<CrmAiChatLog>> myChats() {
        return Result.ok(aiChatService.myChats());
    }
}
