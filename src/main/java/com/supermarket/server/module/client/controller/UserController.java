package com.supermarket.server.module.client.controller;

import com.supermarket.server.common.dto.*;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.client.entity.ClientUser;
import com.supermarket.server.module.client.service.ClientUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private ClientUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ==========================================
    //               辅助方法
    // ==========================================
    /**
     * 提取 Token 中的 UserID，避免重复写代码
     */
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        // 这里假设 JwtUtil 解析失败会抛出异常，由全局异常处理捕获
        return jwtUtil.parseToken(token).get("userId", Long.class);
    }

    // ==========================================
    //               核心接口
    // ==========================================

    @GetMapping("/sendCode")
    public Result<String> sendCode(@RequestParam String phone) {
        userService.sendCode(phone);
        return Result.success("发送成功");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest req) {
        userService.register(req);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest req) {
        LoginResult result = userService.login(req);
        return Result.success(result);
    }

    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@RequestBody ResetPasswordRequest req) {
        userService.resetPassword(req);
        return Result.success("密码重置成功");
    }

    @GetMapping("/info")
    public Result<ClientUser> getUserInfo(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(userService.getUserInfo(userId));
    }

    @PostMapping("/updateInfo")
    public Result<LoginResult> updateInfo(@RequestBody UpdateUserRequest req) {
        LoginResult res = userService.updateInfo(req);
        return Result.success(res);
    }

    // ==========================================
    //               钱包与会员接口
    // ==========================================

    /**
     * 充值接口 (合并优化版)
     * 前端传参: { "amount": 100 }
     */
    @PostMapping("/topUp")
    public Result<BigDecimal> topUp(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        // 安全地获取金额 (防止 JSON 类型转换报错)
        Object amountObj = params.get("amount");
        if (amountObj == null) return Result.error("请输入金额");

        BigDecimal amount = new BigDecimal(amountObj.toString());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }

        try {
            // 调用 Service 的 topUp (返回最新余额)
            BigDecimal newBalance = userService.topUp(userId, amount);
            return Result.success(newBalance);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提现接口
     * 前端传参: { "amount": 50 }
     */
    @PostMapping("/withdraw")
    public Result<BigDecimal> withdraw(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        Object amountObj = params.get("amount");
        if (amountObj == null) return Result.error("请输入金额");

        BigDecimal amount = new BigDecimal(amountObj.toString());
        if (amount.compareTo(BigDecimal.ONE) < 0) return Result.error("最小提现1元");

        try {
            BigDecimal bal = userService.withdraw(userId, amount);
            return Result.success(bal);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 激活会员接口
     */
    @PostMapping("/activateMember")
    public Result<String> activateMember(HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            String memberNo = userService.activateMember(userId);
            return Result.success(memberNo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}