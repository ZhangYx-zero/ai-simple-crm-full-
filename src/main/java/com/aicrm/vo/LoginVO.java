package com.aicrm.vo;

import com.aicrm.entity.SysUser;
import lombok.Data;

/**
 * 登录成功返回：token + 当前用户信息（含 role，前端据此控制菜单/按钮）
 */
@Data
public class LoginVO {

    /** token 的 key，前端用它做请求头，值为固定 "Authorization" */
    private String tokenName;

    /** token 值，前端请求时放到 Header: Authorization=<token> */
    private String token;

    /** 当前登录用户（password 已被 @JsonIgnore 隐藏） */
    private SysUser user;

    public LoginVO() {
    }

    public LoginVO(String tokenName, String token, SysUser user) {
        this.tokenName = tokenName;
        this.token = token;
        this.user = user;
    }
}
