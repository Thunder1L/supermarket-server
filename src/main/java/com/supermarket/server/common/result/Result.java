package com.supermarket.server.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果类
 * 前端收到的 JSON 格式: { "code": 0, "msg": "success", "data": ... }
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; // 状态码: 0成功，1失败
    private String msg;   // 提示信息
    private T data;       // 返回的数据

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功 - 带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    /**
     * 成功 - 不带数据
     */
    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    /**
     * 失败 - 带错误信息
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(1, msg, null);
    }
}