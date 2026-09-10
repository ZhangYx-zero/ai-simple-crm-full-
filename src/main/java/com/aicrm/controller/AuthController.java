package com.aicrm.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.aicrm.common.Result;
import com.aicrm.dto.LoginDTO;
import com.aicrm.entity.SysUser;
import com.aicrm.service.AuthService;
import com.aicrm.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录（公开）/ 登出 / 当前用户
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录：公开接口（已在 SaTokenConfig 放行） */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok("登录成功", authService.login(dto));
    }

    /** 登出：需登录 */
    @PostMapping("/logout")
    @SaCheckLogin
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    /** 当前登录人：需登录 */
    @GetMapping("/me")
    @SaCheckLogin
    public Result<SysUser> me() {
        return Result.ok(authService.me());
    }
}
