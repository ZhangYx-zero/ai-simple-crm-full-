package com.aicrm.common;

import lombok.Getter;

/**
 * 业务异常：在 Service 里主动抛出（如"用户名或密码错误""无权访问该客户"），
 * 由全局异常处理器统一转成 Result 返回给前端。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
