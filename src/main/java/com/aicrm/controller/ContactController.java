package com.aicrm.controller;

import com.aicrm.common.Result;
import com.aicrm.dto.ContactDTO;
import com.aicrm.entity.CrmContact;
import com.aicrm.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 联系人接口（挂在客户下：/api/customers/{customerId}/contacts）。
 */
@RestController
@RequestMapping("/api/customers/{customerId}/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /** 某客户下的联系人列表 */
    @GetMapping
    public Result<List<CrmContact>> list(@PathVariable Long customerId) {
        return Result.ok(contactService.listByCustomer(customerId));
    }

    /** 给某客户新增联系人 */
    @PostMapping
    public Result<CrmContact> add(@PathVariable Long customerId, @Valid @RequestBody ContactDTO dto) {
        return Result.ok("创建成功", contactService.add(customerId, dto));
    }

    /** 编辑联系人 */
    @PutMapping("/{id}")
    public Result<CrmContact> update(@PathVariable Long customerId,
                                     @PathVariable Long id,
                                     @Valid @RequestBody ContactDTO dto) {
        return Result.ok("更新成功", contactService.update(customerId, id, dto));
    }

    /** 删除联系人（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long customerId, @PathVariable Long id) {
        contactService.delete(customerId, id);
        return Result.ok();
    }
}
