package com.supermarket.server.common.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String password;
    private String code;       // 验证码
    private String loginType;  // "password" 或 "code"
}