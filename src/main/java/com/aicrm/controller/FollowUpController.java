package com.aicrm.controller;

import com.aicrm.common.Result;
import com.aicrm.dto.FollowUpDTO;
import com.aicrm.entity.CrmFollowUp;
import com.aicrm.service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 跟进记录接口。
 */
@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    /** 跟进列表：可按 customerId 或 opportunityId 筛选；销售无筛选只见自己客户 */
    @GetMapping
    public Result<List<CrmFollowUp>> list(@RequestParam(required = false) Long customerId,
                                          @RequestParam(required = false) Long opportunityId) {
        return Result.ok(followUpService.list(customerId, opportunityId));
    }

    /** 新增跟进 */
    @PostMapping
    public Result<CrmFollowUp> add(@Valid @RequestBody FollowUpDTO dto) {
        return Result.ok("添加成功", followUpService.add(dto));
    }
}
