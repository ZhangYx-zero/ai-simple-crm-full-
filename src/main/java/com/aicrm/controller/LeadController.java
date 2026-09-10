package com.aicrm.controller;

import com.aicrm.common.PageResult;
import com.aicrm.common.Result;
import com.aicrm.dto.LeadDTO;
import com.aicrm.entity.CrmLead;
import com.aicrm.service.LeadService;
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

/**
 * 线索接口。
 */
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    /** 线索分页列表（/api/leads 与 /api/leads/page 均可访问，见开发计划） */
    @GetMapping({"/page", ""})
    public Result<PageResult<CrmLead>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long ownerId) {
        return Result.ok(leadService.page(name, company, status, ownerId, pageNum, pageSize));
    }

    /** 新增线索 */
    @PostMapping
    public Result<CrmLead> create(@Valid @RequestBody LeadDTO dto) {
        return Result.ok("创建成功", leadService.create(dto));
    }

    /** 编辑线索 */
    @PutMapping("/{id}")
    public Result<CrmLead> update(@PathVariable Long id, @Valid @RequestBody LeadDTO dto) {
        return Result.ok("更新成功", leadService.update(id, dto));
    }

    /** 删除线索 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        leadService.delete(id);
        return Result.ok();
    }

    /** 线索转客户（幂等：已转化再调会被拒） */
    @PostMapping("/{id}/convert")
    public Result<Void> convert(@PathVariable Long id) {
        leadService.convert(id);
        return Result.success("转客户成功");
    }
}
