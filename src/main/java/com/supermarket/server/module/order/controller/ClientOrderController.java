package com.supermarket.server.module.order.controller;

import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.order.entity.Orders;
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

    private Long getUserId(HttpServletRequest request) {
        return jwtUtil.parseToken(request.getHeader("Authorization")).get("userId", Long.class);
    }

    // 列表
    @GetMapping("/list")
    public Result<List<Orders>> list(@RequestParam(required = false) Integer status, HttpServletRequest request) {
        return Result.success(orderService.getMyOrders(getUserId(request), status));
    }

    // 支付
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getUserId(request); // 假设你封装了 getUserId

        String orderNo = params.get("orderNo");
        String password = params.get("password");

        orderService.payOrder(userId, orderNo, password);

        return Result.success("支付成功");
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