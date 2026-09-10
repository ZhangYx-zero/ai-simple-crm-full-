package com.aicrm.service;

import com.aicrm.common.BusinessException;
import com.aicrm.common.CurrentUser;
import com.aicrm.common.PageResult;
import com.aicrm.common.StageConstants;
import com.aicrm.dto.OpportunityDTO;
import com.aicrm.dto.StageChangeDTO;
import com.aicrm.entity.CrmCustomer;
import com.aicrm.entity.CrmFollowUp;
import com.aicrm.entity.CrmOpportunity;
import com.aicrm.mapper.CrmFollowUpMapper;
import com.aicrm.mapper.CrmOpportunityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商机服务 —— 重点：阶段状态机。
 *
 * 状态机规则（写进 Service 而不是散落在 Controller）：
 *   1) 终态（5赢单/6输单）不可再变更；
 *   2) 不能回退（目标 < 当前）——唯一例外：任意阶段可跳到 6(输单)；
 *   3) 赢单(5)：win_rate 自动置 100（可手动覆盖）；
 *   4) 输单(6)：记录 fail_reason；
 *   5) 每次变更自动插入一条 type=系统 的跟进记录，形成商机时间线。
 */
@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final CrmOpportunityMapper opportunityMapper;
    private final CrmFollowUpMapper followUpMapper;
    private final CustomerService customerService;

    /** 商机分页列表（数据权限：SALES 只看自己名下商机） */
    public PageResult<CrmOpportunity> page(String name, Long customerId, Integer stage, Long ownerId,
                                           long pageNum, long pageSize) {
        LambdaQueryWrapper<CrmOpportunity> qw = Wrappers.lambdaQuery();
        qw.like(StringUtils.hasText(name), CrmOpportunity::getName, name)
          .eq(customerId != null, CrmOpportunity::getCustomerId, customerId)
          .eq(stage != null, CrmOpportunity::getStage, stage);
        if (CurrentUser.isSales()) {
            qw.eq(CrmOpportunity::getOwnerId, CurrentUser.id());
        } else if (ownerId != null) {
            qw.eq(CrmOpportunity::getOwnerId, ownerId);
        }
        qw.orderByDesc(CrmOpportunity::getCreateTime);
        return PageResult.of(opportunityMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    /** 新增商机（必须先能访问所属客户；归属默认跟客户走） */
    public CrmOpportunity create(OpportunityDTO dto) {
        // 校验客户可访问，同时拿到客户归属
        CrmCustomer customer = customerService.getAccessible(dto.getCustomerId());

        CrmOpportunity opp = new CrmOpportunity();
        opp.setName(dto.getName());
        opp.setCustomerId(dto.getCustomerId());
        opp.setAmount(dto.getAmount());
        opp.setExpectDate(dto.getExpectDate());

        // 归属：SALES 一律 = 客户归属；管理员可显式指定
        if (CurrentUser.isSales() || dto.getOwnerId() == null) {
            opp.setOwnerId(customer.getOwnerId() == null ? CurrentUser.id() : customer.getOwnerId());
        } else {
            opp.setOwnerId(dto.getOwnerId());
        }
        opp.setStage(dto.getStage() == null ? StageConstants.CONTACT : dto.getStage());
        opp.setWinRate(dto.getWinRate() == null ? 10 : dto.getWinRate());
        opportunityMapper.insert(opp);
        return opp;
    }

    /**
     * 商机编辑：只允许改基本信息（name/amount/expectDate/winRate；管理员可改 ownerId）。
     *  - stage 变更必须走 /{id}/stage（状态机是唯一入口），这里直接拒绝，防止绕过校验；
     *  - customerId 不允许改（商机挂在客户下，改了会破坏归属继承与数据权限）。
     */
    public CrmOpportunity update(Long id, OpportunityDTO dto) {
        CrmOpportunity opp = getAccessible(id);

        if (dto.getCustomerId() != null && !dto.getCustomerId().equals(opp.getCustomerId())) {
            throw new BusinessException("商机所属客户不可修改");
        }
        if (dto.getStage() != null && !dto.getStage().equals(opp.getStage())) {
            throw new BusinessException("阶段变更请使用阶段推进接口");
        }

        if (StringUtils.hasText(dto.getName())) opp.setName(dto.getName());
        if (dto.getAmount() != null) opp.setAmount(dto.getAmount());
        if (dto.getExpectDate() != null) opp.setExpectDate(dto.getExpectDate());
        if (dto.getWinRate() != null) opp.setWinRate(dto.getWinRate());
        // 归属：SALES 不能动；MANAGER/ADMIN 可显式改，否则保持原归属
        if (!CurrentUser.isSales() && dto.getOwnerId() != null) {
            opp.setOwnerId(dto.getOwnerId());
        }
        opportunityMapper.updateById(opp);
        return opp;
    }

    /** 商机删除（逻辑删除；SALES 只能删自己名下） */
    public void delete(Long id) {
        getAccessible(id);
        opportunityMapper.deleteById(id);
    }

    /** 阶段推进（状态机核心，见类注释） */
    @Transactional
    public CrmOpportunity changeStage(Long id, StageChangeDTO dto) {
        CrmOpportunity opp = getAccessible(id);
        int cur = opp.getStage() == null ? StageConstants.CONTACT : opp.getStage();
        int target = dto.getTargetStage();

        // 基本校验
        if (target < StageConstants.CONTACT || target > StageConstants.LOSE) {
            throw new BusinessException("无效的阶段值：" + target);
        }
        if (StageConstants.isEnd(cur)) {
            throw new BusinessException("商机已结束（" + StageConstants.name(cur) + "），不能变更");
        }
        if (target == cur) {
            throw new BusinessException("商机已在「" + StageConstants.name(cur) + "」阶段");
        }
        if (target < cur && target != StageConstants.LOSE) {
            throw new BusinessException("商机阶段不能回退（除非跳到输单）");
        }

        // 终态特殊处理
        if (target == StageConstants.WIN) {
            opp.setWinRate(dto.getWinRate() == null ? 100 : dto.getWinRate());   // 赢单率置100，可覆盖
        }
        if (target == StageConstants.LOSE) {
            opp.setFailReason(dto.getFailReason());                               // 记录输单原因
        }
        opp.setStage(target);
        opportunityMapper.updateById(opp);

        // 自动插入"系统"跟进记录，形成时间线
        CrmFollowUp fu = new CrmFollowUp();
        fu.setCustomerId(opp.getCustomerId());
        fu.setOpportunityId(opp.getId());
        fu.setType("系统");
        fu.setContent("商机阶段变更: " + StageConstants.name(cur) + " → " + StageConstants.name(target));
        fu.setCreateBy(CurrentUser.id());
        followUpMapper.insert(fu);

        return opp;
    }

    /** 某商机的时间线（跟进记录） */
    public List<CrmFollowUp> followUps(Long opportunityId) {
        getAccessible(opportunityId);
        return followUpMapper.selectList(Wrappers.<CrmFollowUp>lambdaQuery()
                .eq(CrmFollowUp::getOpportunityId, opportunityId)
                .orderByAsc(CrmFollowUp::getCreateTime));
    }

    /** 商机越权守卫：SALES 只能操作自己名下商机 */
    public CrmOpportunity getAccessible(Long id) {
        CrmOpportunity opp = opportunityMapper.selectById(id);
        if (opp == null) {
            throw new BusinessException("商机不存在或已删除");
        }
        if (CurrentUser.isSales()
                && (opp.getOwnerId() == null || !opp.getOwnerId().equals(CurrentUser.id()))) {
            throw new BusinessException("无权操作该商机");
        }
        return opp;
    }
}
