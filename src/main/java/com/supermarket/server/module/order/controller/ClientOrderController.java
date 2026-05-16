package com.supermarket.server.module.order.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.order.entity.Orders;
import com.supermarket.server.module.order.mapper.OrdersMapper;
import com.supermarket.server.module.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class ClientOrderController {

    @Autowired private OrderService orderService;
    @Autowired private JwtUtil jwtUtil;

    // 🚨 新增注入：为了在 mockPay 中直接更新订单状态
    @Autowired private OrdersMapper ordersMapper;

    private Long getUserId(HttpServletRequest request) {
        return jwtUtil.parseToken(request.getHeader("Authorization")).get("userId", Long.class);
    }

    // 列表
    @GetMapping("/list")
    public Result<List<Orders>> list(@RequestParam(required = false) Integer status, HttpServletRequest request) {
        return Result.success(orderService.getMyOrders(getUserId(request), status));
    }

    // 原有的 /pay 接口保留（以防你其他地方用到）
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        String orderNo = params.get("orderNo");
        String password = params.get("password");

        orderService.payOrder(userId, orderNo, password);

        return Result.success("支付成功");
    }

    // ==========================================
    // 🚨 新增：专门配合前端二维码收银台的模拟支付接口
    // ==========================================
    @PostMapping("/mockPay")
    public Result<String> mockPay(@RequestBody Map<String, Object> params) {
        // 从请求体中安全地取出前端传过来的 orderId
        Long orderId = Long.valueOf(params.get("orderId").toString());

        // 乐观锁：只允许更新状态为 0 (待付款) 的订单
        UpdateWrapper<Orders> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", orderId)
                .eq("status", 0)
                .set("status", 1); // 1: 支付成功，流转为已支付待处理

        int rows = ordersMapper.update(null, updateWrapper);
        if (rows > 0) {
            return Result.success("模拟支付成功，订单已流转为已支付状态");
        } else {
            return Result.error("支付失败，订单状态异常或已过期");
        }
    }

    // 取消
    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id, HttpServletRequest request) {
        orderService.cancelOrder(getUserId(request), id);
        return Result.success("订单已取消");
    }

    // 确认收货
    @PostMapping("/confirm/{id}")
    public Result<String> confirm(@PathVariable Long id, HttpServletRequest request) {
        orderService.confirmReceive(getUserId(request), id);
        return Result.success("交易完成");
    }

    @PostMapping("/afterSales")
    public Result<String> afterSales(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = params.get("reason").toString();
        orderService.applyAfterSales(getUserId(request), orderId, reason);
        return Result.success("申请已提交");
    }
}