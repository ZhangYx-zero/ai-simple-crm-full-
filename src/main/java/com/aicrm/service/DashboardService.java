package com.aicrm.service;

import com.aicrm.common.CurrentUser;
import com.aicrm.common.StageConstants;
import com.aicrm.mapper.CrmOpportunityMapper;
import com.aicrm.vo.FunnelRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页看板服务。
 * 销售漏斗：按商机阶段统计数量与金额，直观看出"每个阶段有多少单子、多少钱"。
 * 数据权限：SALES 只统计自己名下；管理员可不传 ownerId 统计全公司，或传 ownerId 看某个人。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CrmOpportunityMapper opportunityMapper;

    public List<FunnelRow> funnel(Long ownerId) {
        List<FunnelRow> rows;
        if (CurrentUser.isSales()) {
            // 销售：强制只统计自己的（无视入参 ownerId）
            rows = opportunityMapper.funnelByOwner(CurrentUser.id());
        } else if (ownerId != null) {
            rows = opportunityMapper.funnelByOwner(ownerId);
        } else {
            rows = opportunityMapper.funnelAll();
        }
        // 阶段中文名在这里补，不在 SQL 里拼
        rows.forEach(r -> r.setStageName(StageConstants.name(r.getStage())));
        return rows;
    }
}
