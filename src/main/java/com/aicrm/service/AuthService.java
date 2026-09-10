package com.aicrm.service;

import cn.dev33.satoken.stp.StpUtil;
import com.aicrm.common.BusinessException;
import com.aicrm.dto.LoginDTO;
import com.aicrm.entity.SysUser;
import com.aicrm.mapper.SysUserMapper;
import com.aicrm.vo.LoginVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务：登录 / 登出 / 当前用户。
 * 登录流程：
 *   1) 按用户名查用户（sys_user.username 唯一）；
 *   2) BCrypt 校验密码（不比对原文，matches 重新加密后比对）；
 *   3) StpUtil.login(用户id) 签发 token；
 *   4) 把角色放进会话，供后续数据权限用。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, dto.getUsername()));

        // 用户不存在 / 密码错误统一提示，避免暴露"账号是否存在"
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已停用，请联系管理员");
        }

        // Sa-Token 登录：会话与 token 存在 Redis（见 SaTokenRedisConfig）
        StpUtil.login(user.getId());
        // 把角色写入会话，CurrentUser.role() 读取
        StpUtil.getSession().set("role", user.getRole());

        return new LoginVO(StpUtil.getTokenName(), StpUtil.getTokenValue(), user);
    }

    public void logout() {
        StpUtil.logout();
    }

    public SysUser me() {
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
