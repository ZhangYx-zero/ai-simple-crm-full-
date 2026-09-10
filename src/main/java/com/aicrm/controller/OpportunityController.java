package com.aicrm.controller;

import com.aicrm.common.PageResult;
import com.aicrm.common.Result;
import com.aicrm.dto.OpportunityDTO;
import com.aicrm.dto.StageChangeDTO;
import com.aicrm.entity.CrmFollowUp;
import com.aicrm.entity.CrmOpportunity;
import com.aicrm.service.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商机接口。
 */
@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    /** 商机分页列表（可按 customerId / stage 筛；/api/opportunities 与 /api/opportunities/page 均可访问） */
    @GetMapping({"/page", ""})
    public Result<PageResult<CrmOpportunity>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer stage,
            @RequestParam(required = false) Long ownerId) {
        return Result.ok(opportunityService.page(name, customerId, stage, ownerId, pageNum, pageSize));
    }

    /** 新增商机 */
    @PostMapping
    public Result<CrmOpportunity> create(@Valid @RequestBody OpportunityDTO dto) {
        return Result.ok("创建成功", opportunityService.create(dto));
    }

    /** 编辑商机（基本信息；阶段变更请走 /{id}/stage） */
    @PutMapping("/{id}")
    public Result<CrmOpportunity> update(@PathVariable Long id, @Valid @RequestBody OpportunityDTO dto) {
        return Result.ok("更新成功", opportunityService.update(id, dto));
    }

    /** 删除商机（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return Result.ok();
    }

    /** 商机阶段推进（状态机） */
    @PutMapping("/{id}/stage")
    public Result<CrmOpportunity> changeStage(@PathVariable Long id,
                                              @Valid @RequestBody StageChangeDTO dto) {
        return Result.ok("阶段更新成功", opportunityService.changeStage(id, dto));
    }

    /** 商机时间线 */
    @GetMapping("/{id}/follow-ups")
    public Result<List<CrmFollowUp>> followUps(@PathVariable Long id) {
        return Result.ok(opportunityService.followUps(id));
    }
}
