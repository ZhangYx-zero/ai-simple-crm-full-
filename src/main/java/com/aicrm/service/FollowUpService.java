package com.aicrm.service;

import com.aicrm.common.BusinessException;
import com.aicrm.common.CurrentUser;
import com.aicrm.dto.FollowUpDTO;
import com.aicrm.entity.CrmCustomer;
import com.aicrm.entity.CrmFollowUp;
import com.aicrm.entity.CrmOpportunity;
import com.aicrm.mapper.CrmFollowUpMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 跟进记录服务。
 * crm_follow_up 表没有 owner_id，它的数据权限「继承」自所属客户/商机 ——
 * 所以查询/新增都先校验对应客户或商机是否可访问。
 */
@Service
@RequiredArgsConstructor
public class FollowUpService {

    private final CrmFollowUpMapper followUpMapper;
    private final CustomerService customerService;
    private final OpportunityService opportunityService;

    /**
     * 跟进记录列表（倒序）。
     * 数据权限：SALES 无筛选条件时，只能看【自己名下客户】的跟进。
     */
    public List<CrmFollowUp> list(Long customerId, Long opportunityId) {
        LambdaQueryWrapper<CrmFollowUp> qw = Wrappers.lambdaQuery();

        if (customerId != null) {
            customerService.getAccessible(customerId);                 // 校验客户可见
            qw.eq(CrmFollowUp::getCustomerId, customerId);
        } else if (opportunityId != null) {
            opportunityService.getAccessible(opportunityId);           // 校验商机可见
            qw.eq(CrmFollowUp::getOpportunityId, opportunityId);
        } else if (CurrentUser.isSales()) {
            // 销售不带筛选：限定自己名下客户的跟进（子查询）
            qw.inSql(CrmFollowUp::getCustomerId,
                    "SELECT id FROM crm_customer WHERE deleted = 0 AND owner_id = " + CurrentUser.id());
        }
        qw.orderByDesc(CrmFollowUp::getCreateTime);
        return followUpMapper.selectList(qw);
    }

    /** 新增跟进记录 */
    public CrmFollowUp add(FollowUpDTO dto) {
        Long customerId = dto.getCustomerId();

        // 若只传了商机，从商机反推出客户
        if (customerId == null && dto.getOpportunityId() != null) {
            CrmOpportunity opp = opportunityService.getAccessible(dto.getOpportunityId());
            customerId = opp.getCustomerId();
        }
        if (customerId == null) {
            throw new BusinessException("跟进必须关联客户或商机");
        }
        // 校验客户可见（SALES 不能给别人的客户写跟进）
        CrmCustomer customer = customerService.getAccessible(customerId);

        // 两个都传时，确认商机确实属于该客户
        if (dto.getOpportunityId() != null) {
            CrmOpportunity opp = opportunityService.getAccessible(dto.getOpportunityId());
            if (!opp.getCustomerId().equals(customerId)) {
                throw new BusinessException("商机不属于该客户");
            }
        }

        CrmFollowUp fu = new CrmFollowUp();
        fu.setCustomerId(customerId);
        fu.setOpportunityId(dto.getOpportunityId());
        fu.setType(StringUtils.hasText(dto.getType()) ? dto.getType() : "电话");
        fu.setContent(dto.getContent());
        fu.setNextTime(dto.getNextTime());
        fu.setCreateBy(CurrentUser.id());
        followUpMapper.insert(fu);
        return fu;
    }
}
