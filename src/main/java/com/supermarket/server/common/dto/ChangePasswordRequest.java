package com.supermarket.server.common.dto;

import lombok.Data;

/**
 * 修改密码请求参数
 */
@Data
public class ChangePasswordRequest {
    private Long userId;        // 用户ID (管理员ID或普通用户ID)
    private String oldPassword; // 旧密码
    private String newPassword; // 新密码
}