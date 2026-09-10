package com.aicrm.common;

import lombok.Data;

/**
 * 统一返回体：{ code, message, data }
 * code: 200 成功；400 参数错误；401 未登录；500 业务/系统异常
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /** 成功但无返回数据，可自定义提示语（区别于 ok(String, T)，T 为 null 时避免泛型歧义） */
    public static Result<Void> success(String message) {
        return new Result<>(200, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
