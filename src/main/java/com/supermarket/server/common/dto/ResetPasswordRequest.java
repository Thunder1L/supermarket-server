package com.supermarket.server.common.dto;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String phone;
    private String code;
    private String newPassword;
    private String adminSecret; // 仅管理员用
}