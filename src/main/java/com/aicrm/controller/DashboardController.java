package com.aicrm.controller;

import com.aicrm.common.Result;
import com.aicrm.service.DashboardService;
import com.aicrm.vo.FunnelRow;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页看板接口。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 销售漏斗统计 */
    @GetMapping("/funnel")
    public Result<List<FunnelRow>> funnel(@RequestParam(required = false) Long ownerId) {
        return Result.ok(dashboardService.funnel(ownerId));
    }
}
