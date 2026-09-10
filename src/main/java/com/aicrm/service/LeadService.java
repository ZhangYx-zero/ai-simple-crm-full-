package com.aicrm.service;

import com.aicrm.common.BusinessException;
import com.aicrm.common.CurrentUser;
import com.aicrm.common.PageResult;
import com.aicrm.dto.LeadDTO;
import com.aicrm.entity.CrmContact;
import com.aicrm.entity.CrmCustomer;
import com.aicrm.entity.CrmLead;
import com.aicrm.mapper.CrmContactMapper;
import com.aicrm.mapper.CrmCustomerMapper;
import com.aicrm.mapper.CrmLeadMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 线索服务。线索（Leads）是还没转成客户的潜在对象。
 * 核心动作：convert —— 把线索转成「客户 + 联系人」，同时线索状态置为 2(已转客户)。
 */
@Service
@RequiredArgsConstructor
public class LeadService {

    private final CrmLeadMapper leadMapper;
    private final CrmCustomerMapper customerMapper;
    private final CrmContactMapper contactMapper;

    /** 线索分页列表（数据权限：SALES 只看自己） */
    public PageResult<CrmLead> page(String name, String company, Integer status, Long ownerId,
                                    long pageNum, long pageSize) {
        LambdaQueryWrapper<CrmLead> qw = Wrappers.lambdaQuery();
        qw.like(StringUtils.hasText(name), CrmLead::getName, name)
          .like(StringUtils.hasText(company), CrmLead::getCompany, company)
          .eq(status != null, CrmLead::getStatus, status);
        if (CurrentUser.isSales()) {
            qw.eq(CrmLead::getOwnerId, CurrentUser.id());
        } else if (ownerId != null) {
            qw.eq(CrmLead::getOwnerId, ownerId);
        }
        qw.orderByDesc(CrmLead::getCreateTime);
        return PageResult.of(leadMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    /** 新增线索 */
    public CrmLead create(LeadDTO dto) {
        CrmLead lead = new CrmLead();
        copyFields(dto, lead);
        if (CurrentUser.isSales() || dto.getOwnerId() == null) {
            lead.setOwnerId(CurrentUser.id());
        } else {
            lead.setOwnerId(dto.getOwnerId());
        }
        if (lead.getStatus() == null) {
            lead.setStatus(0); // 默认：新线索
        }
        leadMapper.insert(lead);
        return lead;
    }

    /** 编辑线索 */
    public CrmLead update(Long id, LeadDTO dto) {
        CrmLead lead = getAccessible(id);
        copyFields(dto, lead);
        leadMapper.updateById(lead);
        return lead;
    }

    /** 删除线索（逻辑删除） */
    public void delete(Long id) {
        getAccessible(id);
        leadMapper.deleteById(id);
    }

    /**
     * 线索转客户（核心事务）：
     *   1) 校验：线索存在 && 未转化（status != 2）；
     *   2) 建客户：客户名 = company（公司）非空 ? company : name；归属 = 线索归属；status = 1 正式客户；
     *   3) 建联系人：姓名 = 线索 name、电话 = 线索 phone；
     *   4) 线索 status = 2（已转客户）。
     * 全程 @Transactional：任何一步失败都整体回滚，不会出现"客户建了但线索没更新"。
     */
    @Transactional
    public void convert(Long id) {
        CrmLead lead = getAccessible(id);
        if (lead.getStatus() != null && lead.getStatus() == 2) {
            throw new BusinessException("线索不存在或已转化");
        }

        // 1) 建客户
        String customerName = StringUtils.hasText(lead.getCompany()) ? lead.getCompany() : lead.getName();
        CrmCustomer customer = new CrmCustomer();
        customer.setName(customerName);
        customer.setOwnerId(lead.getOwnerId());
        customer.setStatus(1);                       // 正式客户
        if (StringUtils.hasText(lead.getSource())) {
            customer.setSource(lead.getSource());
        }
        if (StringUtils.hasText(lead.getPhone())) {
            customer.setPhone(lead.getPhone());
        }
        customerMapper.insert(customer);

        // 2) 建联系人
        CrmContact contact = new CrmContact();
        contact.setCustomerId(customer.getId());
        contact.setName(lead.getName());
        contact.setPhone(lead.getPhone());
        contactMapper.insert(contact);

        // 3) 线索标记已转
        lead.setStatus(2);
        leadMapper.updateById(lead);
    }

    /** 线索越权守卫：SALES 只能操作自己名下线索 */
    public CrmLead getAccessible(Long id) {
        CrmLead lead = leadMapper.selectById(id);
        if (lead == null) {
            throw new BusinessException("线索不存在或已删除");
        }
        if (CurrentUser.isSales()
                && (lead.getOwnerId() == null || !lead.getOwnerId().equals(CurrentUser.id()))) {
            throw new BusinessException("无权操作该线索");
        }
        return lead;
    }

    private void copyFields(LeadDTO dto, CrmLead lead) {
        if (StringUtils.hasText(dto.getName())) lead.setName(dto.getName());
        if (StringUtils.hasText(dto.getCompany())) lead.setCompany(dto.getCompany());
        if (StringUtils.hasText(dto.getPhone())) lead.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getSource())) lead.setSource(dto.getSource());
        if (dto.getStatus() != null) lead.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getRemark())) lead.setRemark(dto.getRemark());
    }
}
