package com.supermarket.server.module.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.module.order.entity.Orders;
import com.supermarket.server.module.order.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/delivery")
public class EmployeeDeliveryController {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 获取待配送的订单列表
     */
    @GetMapping("/list")
    public Result getPendingDeliveryOrders() {
        // 业务逻辑：查询状态为 1 (已付款待发货) 或 2 (已发货待签收) 的订单
        // 如果你的数据库有 delivery_type 字段区分“自提”和“外送”，这里可以加上 .eq("delivery_type", "外送")
        QueryWrapper<Orders> query = new QueryWrapper<>();
        query.in("status", 1, 2)
                .isNotNull("receiver_address") // 必须要有收货地址才是外送
                .orderByAsc("create_time"); // 按时间排序，先下单的先送

        List<Orders> list = ordersMapper.selectList(query);
        return Result.success(list);
    }

    /**
     * 员工确认送达
     */
    @PostMapping("/complete")
    public Result completeDelivery(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());

        // 乐观锁：只允许更新状态为 1 或 2 的订单，将其更新为 3 (已完成)
        UpdateWrapper<Orders> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .in("status", 1, 2)
                .set("status", 3);

        int rows = ordersMapper.update(null, updateWrapper);
        if (rows > 0) {
            return Result.success("订单配送完成！");
        }
        return Result.error("确认失败，订单状态可能已改变");
    }
}