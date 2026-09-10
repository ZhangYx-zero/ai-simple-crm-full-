package com.aicrm.common;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 当前登录人工具类：从 Sa-Token 会话中取当前用户信息。
 * 数据权限（SALES 只看自己）会在 Service 里调用本类判断。
 */
public class CurrentUser {

    /** 当前登录人 id */
    public static Long id() {
        return StpUtil.getLoginIdAsLong();
    }

    /** 当前登录人角色：ADMIN / MANAGER / SALES（登录时写入会话） */
    public static String role() {
        Object role = StpUtil.getSession().get("role");
        return role == null ? null : role.toString();
    }

    /** 是否普通销售（只看自己名下数据） */
    public static boolean isSales() {
        return "SALES".equals(role());
    }

    /** 是否主管/管理员（可看全部数据） */
    public static boolean canViewAll() {
        return "ADMIN".equals(role()) || "MANAGER".equals(role());
    }
}
