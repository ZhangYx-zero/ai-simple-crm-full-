package com.aicrm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户表 sys_user 对应实体。
 * 一个用户三种角色：ADMIN（管理员，看全部）/ MANAGER（主管，看全部）/ SALES（销售，只看自己）。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 密码：只用于登录校验，绝不返回给前端（@JsonIgnore 序列化时直接跳过） */
    @JsonIgnore
    private String password;

    private String nickname;

    /** 角色：ADMIN / MANAGER / SALES */
    private String role;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    private LocalDateTime createTime;
}
