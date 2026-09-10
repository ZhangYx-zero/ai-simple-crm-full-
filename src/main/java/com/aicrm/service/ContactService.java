package com.aicrm.service;

import com.aicrm.common.BusinessException;
import com.aicrm.dto.ContactDTO;
import com.aicrm.entity.CrmContact;
import com.aicrm.mapper.CrmContactMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 联系人服务。
 * 联系人没有独立的 owner，它的可见性继承自所属客户 ——
 * 所以每个操作前都先调 customerService.getAccessible(customerId) 做越权守卫。
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    private final CrmContactMapper contactMapper;
    private final CustomerService customerService;

    /** 某客户下的联系人列表 */
    public List<CrmContact> listByCustomer(Long customerId) {
        customerService.getAccessible(customerId);
        return contactMapper.selectList(Wrappers.<CrmContact>lambdaQuery()
                .eq(CrmContact::getCustomerId, customerId)
                .orderByDesc(CrmContact::getCreateTime));
    }

    /** 给某客户新增联系人 */
    public CrmContact add(Long customerId, ContactDTO dto) {
        customerService.getAccessible(customerId);
        CrmContact cc = new CrmContact();
        cc.setCustomerId(customerId);
        cc.setName(dto.getName());
        cc.setPosition(dto.getPosition());
        cc.setPhone(dto.getPhone());
        cc.setWechat(dto.getWechat());
        cc.setIsDecision(dto.getIsDecision() == null ? 0 : dto.getIsDecision());
        cc.setRemark(dto.getRemark());
        contactMapper.insert(cc);
        return cc;
    }

    /** 编辑联系人（先校验所属客户可访问，再校验联系人的确属于该客户） */
    public CrmContact update(Long customerId, Long id, ContactDTO dto) {
        customerService.getAccessible(customerId);
        CrmContact cc = contactMapper.selectById(id);
        if (cc == null || !customerId.equals(cc.getCustomerId())) {
            throw new BusinessException("联系人不存在或不属于该客户");
        }
        if (StringUtils.hasText(dto.getName())) cc.setName(dto.getName());
        if (StringUtils.hasText(dto.getPosition())) cc.setPosition(dto.getPosition());
        if (StringUtils.hasText(dto.getPhone())) cc.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getWechat())) cc.setWechat(dto.getWechat());
        if (dto.getIsDecision() != null) cc.setIsDecision(dto.getIsDecision());
        if (StringUtils.hasText(dto.getRemark())) cc.setRemark(dto.getRemark());
        contactMapper.updateById(cc);
        return cc;
    }

    /** 删除联系人（逻辑删除） */
    public void delete(Long customerId, Long id) {
        customerService.getAccessible(customerId);
        CrmContact cc = contactMapper.selectById(id);
        if (cc == null || !customerId.equals(cc.getCustomerId())) {
            throw new BusinessException("联系人不存在或不属于该客户");
        }
        contactMapper.deleteById(id);
    }
}
