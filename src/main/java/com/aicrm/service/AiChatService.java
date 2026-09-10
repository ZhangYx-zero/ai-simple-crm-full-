package com.aicrm.service;

import com.aicrm.ai.AiClient;
import com.aicrm.common.CurrentUser;
import com.aicrm.common.StageConstants;
import com.aicrm.dto.AiChatDTO;
import com.aicrm.entity.CrmAiChatLog;
import com.aicrm.entity.CrmCustomer;
import com.aicrm.entity.CrmFollowUp;
import com.aicrm.entity.CrmOpportunity;
import com.aicrm.mapper.CrmAiChatLogMapper;
import com.aicrm.mapper.CrmFollowUpMapper;
import com.aicrm.mapper.CrmOpportunityMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * AI 助手服务。
 * 做的事情：
 *   1) 若带了 customerId/opportunityId，先做数据权限守卫（复用 CustomerService/OpportunityService）；
 *   2) 把客户档案 + 关联商机 + 最近跟进记录拼成上下文；
 *   3) 调 AiClient（真实或模拟）；
 *   4) 把问答落库 crm_ai_chat_log，供"我的提问记录"查询。
 */
@Service
@RequiredArgsConstructor
public class AiChatService {

    /** 系统提示词：给 AI 定角色和回答约束 */
    private static final String SYSTEM_PROMPT = """
            你是一位资深的B2B销售教练，擅长销售方法论（如 SPIN、FABE）与大客户跟进。
            你会收到"问题"和系统里已有的"客户/商机/跟进上下文"。请：
            1. 基于上下文给出可落地的销售建议，不要空谈；
            2. 上下文不足时，明确告诉销售还缺哪些信息、下一步怎么补；
            3. 用中文回答，分点、控制在 300 字以内，语气务实。
            """;

    private final AiClient aiClient;
    private final CustomerService customerService;
    private final OpportunityService opportunityService;
    private final CrmOpportunityMapper opportunityMapper;
    private final CrmFollowUpMapper followUpMapper;
    private final CrmAiChatLogMapper aiChatLogMapper;

    public String chat(AiChatDTO dto) {
        String ctx = buildContext(dto);

        StringBuilder user = new StringBuilder("问题：").append(dto.getQuestion());
        if (!ctx.isEmpty()) {
            user.append("\n\n参考上下文（来自 CRM 系统）：\n").append(ctx);
        }

        String answer = aiClient.chat(SYSTEM_PROMPT, user.toString());

        // 落库
        CrmAiChatLog log = new CrmAiChatLog();
        log.setUserId(CurrentUser.id());
        log.setCustomerId(dto.getCustomerId());
        log.setOpportunityId(dto.getOpportunityId());
        log.setQuestion(dto.getQuestion());
        log.setAnswer(answer);
        log.setModel(aiClient.name());
        aiChatLogMapper.insert(log);

        return answer;
    }

    /** 当前登录人的提问历史（最近 50 条） */
    public List<CrmAiChatLog> myChats() {
        return aiChatLogMapper.selectList(Wrappers.<CrmAiChatLog>lambdaQuery()
                .eq(CrmAiChatLog::getUserId, CurrentUser.id())
                .orderByDesc(CrmAiChatLog::getCreateTime)
                .last("LIMIT 50"));
    }

    /** 拼上下文（含数据权限校验，越权直接抛异常） */
    private String buildContext(AiChatDTO dto) {
        StringBuilder ctx = new StringBuilder();
        Long customerId = dto.getCustomerId();

        // 只给了商机 -> 先校验商机可见，再反查客户
        if (customerId == null && dto.getOpportunityId() != null) {
            CrmOpportunity opp = opportunityService.getAccessible(dto.getOpportunityId());
            customerId = opp.getCustomerId();
            ctx.append("【关联商机】").append(opp.getName())
               .append("｜阶段：").append(StageConstants.name(opp.getStage()))
               .append("｜金额：").append(opp.getAmount()).append("元\n");
        }

        if (customerId == null) {
            return ctx.toString();   // 没带上下文，AI 只能泛泛回答
        }

        // 客户档案（同时校验可见性）
        CrmCustomer c = customerService.getAccessible(customerId);
        ctx.append("【客户】").append(c.getName());
        if (StringUtils.hasText(c.getIndustry())) ctx.append("｜行业：").append(c.getIndustry());
        if (StringUtils.hasText(c.getLevel())) ctx.append("｜等级：").append(c.getLevel());
        ctx.append("｜状态：").append(customerStatusName(c.getStatus()));
        if (StringUtils.hasText(c.getPhone())) ctx.append("｜电话：").append(c.getPhone());
        ctx.append("\n");

        // 关联商机（未单独指定商机时带上）
        if (dto.getOpportunityId() == null) {
            List<CrmOpportunity> opps = opportunityMapper.selectList(Wrappers.<CrmOpportunity>lambdaQuery()
                    .eq(CrmOpportunity::getCustomerId, customerId)
                    .orderByDesc(CrmOpportunity::getCreateTime));
            if (!opps.isEmpty()) {
                ctx.append("【名下商机】\n");
                for (CrmOpportunity opp : opps) {
                    ctx.append("  · ").append(opp.getName())
                       .append("｜").append(StageConstants.name(opp.getStage()))
                       .append("｜").append(opp.getAmount()).append("元\n");
                }
            }
        }

        // 最近跟进记录（最多 5 条）
        List<CrmFollowUp> fus = followUpMapper.selectList(Wrappers.<CrmFollowUp>lambdaQuery()
                .eq(CrmFollowUp::getCustomerId, customerId)
                .orderByDesc(CrmFollowUp::getCreateTime));
        if (!fus.isEmpty()) {
            ctx.append("【最近跟进】\n");
            fus.stream().limit(5).forEach(fu -> ctx.append("  · ")
                    .append(fu.getCreateTime() == null ? "" : fu.getCreateTime().toString().substring(0, 16))
                    .append(" [").append(fu.getType()).append("] ").append(fu.getContent()).append("\n"));
        }
        return ctx.toString();
    }

    private String customerStatusName(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "正式";
            case 2 -> "流失";
            default -> "潜在";
        };
    }
}
