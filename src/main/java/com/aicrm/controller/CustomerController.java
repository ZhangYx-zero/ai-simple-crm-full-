package com.aicrm.controller;

import com.aicrm.common.PageResult;
import com.aicrm.common.Result;
import com.aicrm.dto.CustomerDTO;
import com.aicrm.dto.TransferDTO;
import com.aicrm.entity.CrmCustomer;
import com.aicrm.service.CustomerService;
import com.aicrm.vo.CustomerDetailVO;
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
 * 客户接口。所有 /api/** 均已被 SaTokenConfig 拦截要求登录。
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /** 客户分页列表（name/industry/level 模糊或精确筛，ownerId 仅管理员可用）。
     *  提供 /api/customers 与 /api/customers/page 两种路径（开发计划按 /page 命名）。 */
    @GetMapping({"/page", ""})
    public Result<PageResult<CrmCustomer>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Long ownerId) {
        return Result.ok(customerService.page(name, industry, level, ownerId, pageNum, pageSize));
    }

    /** 客户详情（含联系人） */
    @GetMapping("/{id}")
    public Result<CustomerDetailVO> detail(@PathVariable Long id) {
        return Result.ok(customerService.detail(id));
    }

    /** 新增客户 */
    @PostMapping
    public Result<CrmCustomer> create(@Valid @RequestBody CustomerDTO dto) {
        return Result.ok("创建成功", customerService.create(dto));
    }

    /** 编辑客户 */
    @PutMapping("/{id}")
    public Result<CrmCustomer> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        return Result.ok("更新成功", customerService.update(id, dto));
    }

    /** 删除客户（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.ok();
    }

    /** 转移归属（仅 ADMIN/MANAGER） */
    @PutMapping("/{id}/transfer")
    public Result<CrmCustomer> transfer(@PathVariable Long id, @Valid @RequestBody TransferDTO dto) {
        return Result.ok("转移成功", customerService.transfer(id, dto.getNewOwnerId()));
    }
}
