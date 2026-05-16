package com.supermarket.server.module.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.module.order.entity.Orders;
import com.supermarket.server.module.order.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee/order")
public class EmployeeOrderController {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 门店员工扫码核销订单
     * 业务逻辑：只有状态为 1(已支付待发货/待自提) 或 2(已发货待签收) 的订单才允许被核销完成。
     */
    @PostMapping("/verify")
    public Result verifyOrder(@RequestParam("orderNo") String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error("订单号不能为空");
        }

        // 1. 查询订单是否存在
        Orders order = ordersMapper.selectOne(new QueryWrapper<Orders>().eq("order_no", orderNo));
        if (order == null) {
            return Result.error("未查询到该订单，请核对订单号");
        }

        // 2. 防御性拦截：检查订单状态是否允许核销
        Integer status = order.getStatus();
        // 假设规范：0待付款，1已付款待处理，2已发货/待提货，3已完成，4已取消，5售后中
        if (status == 3) {
            return Result.error("该订单已核销完毕，请勿重复操作！");
        }
        if (status == 0 || status == 4 || status == 5) {
            return Result.error("订单状态异常（待付款/已取消/售后中），无法核销！");
        }

        // 3. 执行核销：使用 UpdateWrapper 保证并发安全
        // 只有当状态仍为旧状态时才更新为已完成(3)，防止瞬间多人同时扫码
        UpdateWrapper<Orders> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", order.getId())
                .eq("status", status)
                .set("status", 3); // 3 代表订单已圆满完成

        int rows = ordersMapper.update(null, updateWrapper);
        if (rows > 0) {
            return Result.success("订单核销成功");
        } else {
            return Result.error("核销失败，订单状态可能已发生改变");
        }
    }
}