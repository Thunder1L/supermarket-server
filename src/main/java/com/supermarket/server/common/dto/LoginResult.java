package com.supermarket.server.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true) // 允许链式调用
public class LoginResult {
    private String token;
    private Long id;
    private String username;
    private String avatar;
    private String role; // "USER" or "ADMIN"
    private String phone;
}