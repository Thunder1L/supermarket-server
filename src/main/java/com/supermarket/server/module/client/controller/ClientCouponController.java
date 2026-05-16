package com.supermarket.server.module.client.controller;

import com.supermarket.server.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;

@RestController
// 🚨 注意：这里的路径必须和你前端 @/api/coupon.js 里写的请求路径保持一致！
@RequestMapping("/api/coupon")
public class ClientCouponController {

    /**
     * 获取当前用户的优惠券列表
     * 临时方案：直接返回空列表，打通前端结算页面流程
     */
    @GetMapping("/list") // 🚨 根据你前端实际请求的接口名（如 /list 或 /my）进行修改
    public Result getMyCoupons() {
        // TODO: 未来有空了，再通过 user_coupon 表和 userId 查出真实的优惠券
        return Result.success(new ArrayList<>());
    }
}