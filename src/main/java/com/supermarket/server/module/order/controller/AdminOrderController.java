package com.supermarket.server.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.module.order.entity.Orders;
import com.supermarket.server.module.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    // 分页获取列表
    @GetMapping("/list")
    public Result<IPage<Orders>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo
    ) {
        return Result.success(orderService.getAdminOrderList(page, size, status, orderNo));
    }

    // 发货
    @PostMapping("/ship/{id}")
    public Result<String> ship(@PathVariable Long id) {
        try {
            orderService.shipOrder(id);
            return Result.success("发货成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success("删除成功");
    }
}