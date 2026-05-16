package com.supermarket.server.module.admin.controller;

import com.supermarket.server.common.dto.*;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.module.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 发送验证码
    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestParam String phone) {
        adminService.sendCode(phone);
        return Result.success("发送成功");
    }

    // 注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest req) {
        try {
            adminService.register(req);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 登录
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest req) {
        try {
            LoginResult result = adminService.login(req);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            adminService.resetPassword(req);
            return Result.success("重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        try {
            DashboardStats stats = adminService.getStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计数据失败");
        }
    }

    @PostMapping("/updateInfo")
    public Result<LoginResult> updateInfo(@RequestBody UpdateUserRequest req) {
        try {
            LoginResult res = adminService.updateInfo(req);
            return Result.success(res);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody ChangePasswordRequest req) {
        try {
            adminService.changePassword(req);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}