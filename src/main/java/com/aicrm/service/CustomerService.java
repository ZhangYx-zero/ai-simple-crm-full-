package com.aicrm.service;

import com.aicrm.common.BusinessException;
import com.aicrm.common.CurrentUser;
import com.aicrm.common.PageResult;
import com.aicrm.dto.CustomerDTO;
import com.aicrm.entity.CrmContact;
import com.aicrm.entity.CrmCustomer;
import com.aicrm.mapper.CrmContactMapper;
import com.aicrm.mapper.CrmCustomerMapper;
import com.aicrm.vo.CustomerDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户服务 —— 数据权限的核心演示点。
 *
 * 数据权限规则（和其余列表接口保持一致）：
 *   - SALES（销售）：只能看/改【自己名下】（owner_id = 当前人）的数据，入参 ownerId 直接忽略；
 *   - MANAGER / ADMIN：可看全部，可按 ownerId 筛选、可转移归属。
 *
 * getAccessible(id) 是本模块的【统一越权守卫】：详情/编辑/删除/联系人/跟进/AI 都复用它，
 * 保证"无权访问"的判断只有一份代码。
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CrmCustomerMapper customerMapper;
    private final CrmContactMapper contactMapper;

    /** 分页列表（带数据权限过滤） */
    public PageResult<CrmCustomer> page(String name, String industry, String level, Long ownerId,
                                        long pageNum, long pageSize) {
        LambdaQueryWrapper<CrmCustomer> qw = Wrappers.lambdaQuery();
        qw.like(StringUtils.hasText(name), CrmCustomer::getName, name)
          .eq(StringUtils.hasText(industry), CrmCustomer::getIndustry, industry)
          .eq(StringUtils.hasText(level), CrmCustomer::getLevel, level);
        if (CurrentUser.isSales()) {
            qw.eq(CrmCustomer::getOwnerId, CurrentUser.id());   // 销售强制只看自己
        } else if (ownerId != null) {
            qw.eq(CrmCustomer::getOwnerId, ownerId);            // 管理员/主管可按归属筛
        }
        qw.orderByDesc(CrmCustomer::getCreateTime);
        Page<CrmCustomer> page = customerMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return PageResult.of(page);
    }

    /** 客户详情：客户 + 联系人列表 */
    public CustomerDetailVO detail(Long id) {
        CrmCustomer c = getAccessible(id);
        List<CrmContact> contacts = contactMapper.selectList(Wrappers.<CrmContact>lambdaQuery()
                .eq(CrmContact::getCustomerId, id)
                .orderByDesc(CrmContact::getCreateTime));
        CustomerDetailVO vo = new CustomerDetailVO();
        vo.setCustomer(c);
        vo.setContacts(contacts);
        return vo;
    }

    /** 新增客户 */
    public CrmCustomer create(CustomerDTO dto) {
        CrmCustomer c = new CrmCustomer();
        copyFields(dto, c);
        // 归属：销售强制归自己；管理员不传则也归自己（传了就归指定人）
        if (CurrentUser.isSales() || dto.getOwnerId() == null) {
            c.setOwnerId(CurrentUser.id());
        } else {
            c.setOwnerId(dto.getOwnerId());
        }
        if (c.getStatus() == null) {
            c.setStatus(0); // 默认：潜在客户
        }
        customerMapper.insert(c);
        return c;
    }

    /** 编辑客户（SALES 只能编辑自己名下，getAccessible 保证） */
    public CrmCustomer update(Long id, CustomerDTO dto) {
        CrmCustomer c = getAccessible(id);
        copyFields(dto, c);   // 注意 copyFields 不碰 ownerId：归属只能走 /transfer
        customerMapper.updateById(c);
        return c;
    }

    /** 逻辑删除（SALES 只能删自己名下） */
    public void delete(Long id) {
        getAccessible(id);
        customerMapper.deleteById(id);   // 全局逻辑删除 => 实际执行 UPDATE deleted=1
    }

    /** 归属转移：仅 MANAGER/ADMIN 可用，SALES 一律拒绝 */
    public CrmCustomer transfer(Long id, Long newOwnerId) {
        if (CurrentUser.isSales()) {
            throw new BusinessException("无权转移客户归属");
        }
        CrmCustomer c = customerMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("客户不存在或已删除");
        }
        c.setOwnerId(newOwnerId);
        customerMapper.updateById(c);
        return c;
    }

    /**
     * 统一越权守卫：查客户并校验当前人是否有权访问。
     * SALES 名下无此客户 => 抛业务异常（前端拿 500 + 提示）。
     */
    public CrmCustomer getAccessible(Long id) {
        CrmCustomer c = customerMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("客户不存在或已删除");
        }
        if (CurrentUser.isSales()
                && (c.getOwnerId() == null || !c.getOwnerId().equals(CurrentUser.id()))) {
            throw new BusinessException("无权访问该客户");
        }
        return c;
    }

    /** 把 DTO 里非空的业务字段拷贝到实体（不处理 ownerId，归属单独管理） */
    private void copyFields(CustomerDTO dto, CrmCustomer c) {
        if (StringUtils.hasText(dto.getName())) c.setName(dto.getName());
        if (StringUtils.hasText(dto.getIndustry())) c.setIndustry(dto.getIndustry());
        if (StringUtils.hasText(dto.getLevel())) c.setLevel(dto.getLevel());
        if (StringUtils.hasText(dto.getSource())) c.setSource(dto.getSource());
        if (dto.getStatus() != null) c.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getPhone())) c.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getAddress())) c.setAddress(dto.getAddress());
        if (StringUtils.hasText(dto.getRemark())) c.setRemark(dto.getRemark());
    }
}
