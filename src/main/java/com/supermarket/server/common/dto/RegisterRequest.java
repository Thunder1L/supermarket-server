package com.supermarket.server.common.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String phone;
    private String password;
    private String confirmPassword; // 虽然前端校验了，后端也可以接一下，或者直接忽略
    private String code;            // 手机验证码
    private String role;         // "ADMIN" 或 "EMPLOYEE"
    private String adminSecret;     // 管理员注册口令
    private String storeCode;    // 员工门店授权码
}