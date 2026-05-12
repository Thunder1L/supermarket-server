package com.supermarket.server.module.client.controller;

import com.supermarket.server.common.entity.Message;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.client.service.MessageService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired private MessageService messageService;
    @Autowired private JwtUtil jwtUtil;

    // 辅助方法：解析 Token 获取 Claims
    private Claims getClaims(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("请先登录");
        }
        return jwtUtil.parseToken(token);
    }

    // 1. 发送消息
    @PostMapping("/send")
    public Result<String> send(@RequestBody Message msg, HttpServletRequest request) {
        Claims claims = getClaims(request);
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        msg.setSenderId(userId);
        msg.setSenderRole(role);

        // 逻辑：
        // 1. 普通用户发 -> 默认发给 Admin (ID=1)
        // 2. 管理员发 -> 前端必须在 msg 里传 receiverId (发给哪个用户)
        if ("USER".equals(role)) {
            msg.setReceiverId(1L); // 假设超级管理员ID是1
        } else if (msg.getReceiverId() == null) {
            return Result.error("管理员回复必须指定接收用户ID");
        }

        messageService.send(msg);
        return Result.success("发送成功");
    }

    // 2. 获取我的聊天记录 (用户看自己的，管理员看自己的)
    @GetMapping("/list")
    public Result<List<Message>> list(HttpServletRequest request) {
        Claims claims = getClaims(request);
        Long userId = claims.get("userId", Long.class);
        return Result.success(messageService.getHistory(userId));
    }

    // 3. 获取所有聊天记录 (仅限管理员)
    @GetMapping("/admin/list")
    public Result<List<Message>> adminList(HttpServletRequest request) {
        // 【关键安全检查】
        Claims claims = getClaims(request);
        String role = claims.get("role", String.class);

        if (!"ADMIN".equals(role)) {
            return Result.error("无权访问"); // 拦截普通用户
        }

        return Result.success(messageService.getAllMessages());
    }
}