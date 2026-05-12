package com.supermarket.server.module.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.server.module.client.entity.AfterSales;
import com.supermarket.server.module.client.entity.BankCard;
import com.supermarket.server.module.client.entity.ClientUser;
import com.supermarket.server.module.client.mapper.AfterSalesMapper;
import com.supermarket.server.module.client.mapper.BankCardMapper;
import com.supermarket.server.module.client.mapper.ClientUserMapper;
import com.supermarket.server.module.order.entity.OrderItem;
import com.supermarket.server.module.order.entity.Orders;
import com.supermarket.server.module.order.mapper.OrderItemMapper;
import com.supermarket.server.module.order.mapper.OrdersMapper;
import com.supermarket.server.module.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrdersMapper ordersMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private AfterSalesMapper afterSalesMapper;
    @Autowired private ClientUserMapper userMapper;
    @Autowired private BankCardMapper cardMapper;

    // ==========================================
    //                 用户端功能
    // ==========================================

    /**
     * 查询我的订单列表 (用户)
     */
    public List<Orders> getMyOrders(Long userId, Integer status) {
        QueryWrapper<Orders> query = new QueryWrapper<>();
        query.eq("user_id", userId);

        if (status != null) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");

        List<Orders> orders = ordersMapper.selectList(query);
        fillOrderItems(orders);
        return orders;
    }

    /**
     * 支付订单
     * 【修正】增加了 String password 参数
     * 【修正】将 orderId 改为 String orderNo (更符合支付场景)
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, String orderNo, String password) {
        // 1. 根据订单号查询订单
        Orders order = ordersMapper.selectOne(new QueryWrapper<Orders>().eq("order_no", orderNo));

        // 2. 基础校验
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常或已支付");
        }

        // 3. 验证支付密码
        ClientUser user = userMapper.selectById(userId);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("支付密码错误");
        }

        // 4. 根据支付方式处理
        // 注意：getPayType() 需要你的 Orders 实体类里有 private Integer payType; 字段
        Integer payType = order.getPayType();

        if (payType != null && payType == 3) {
            // --- 余额支付 ---
            if (user.getBalance() == null || user.getBalance().compareTo(order.getTotalAmount()) < 0) {
                throw new RuntimeException("余额不足，请去个人中心充值");
            }
            // 扣款
            user.setBalance(user.getBalance().subtract(order.getTotalAmount()));
            userMapper.updateById(user);

        } else if (payType != null && payType == 4) {
            // --- 银行卡支付 ---
            Long cardCount = cardMapper.selectCount(new QueryWrapper<BankCard>().eq("user_id", userId));
            if (cardCount == 0) {
                throw new RuntimeException("请先绑定银行卡");
            }
            // 模拟银行扣款...
        }
        // payType 1(微信) 和 2(支付宝) 通常直接放行，实际开发中需要验签第三方回调

        // 5. 更新订单状态
        order.setStatus(1); // 1: 待发货
        order.setPaymentTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    /**
     * 取消订单 (0 -> 4)
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Orders order = checkOwner(userId, orderId);
        if (order.getStatus() != 0) throw new RuntimeException("只能取消待付款订单");

        order.setStatus(4); // 4: 已取消
        ordersMapper.updateById(order);
    }

    /**
     * 确认收货 (2 -> 3)
     */
    public void confirmReceive(Long userId, Long orderId) {
        Orders order = checkOwner(userId, orderId);
        if (order.getStatus() != 2) throw new RuntimeException("订单未发货或已完成");

        order.setStatus(3); // 3: 已完成
        ordersMapper.updateById(order);
    }

    /**
     * 申请售后
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyAfterSales(Long userId, Long orderId, String reason) {
        Orders order = checkOwner(userId, orderId);

        // 只有 已发货(2) 或 已完成(3) 可以申请
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            throw new RuntimeException("当前状态无法申请售后");
        }

        // 修改状态为 5 (售后中)
        order.setStatus(5);
        ordersMapper.updateById(order);

        // 插入记录
        AfterSales sales = new AfterSales();
        sales.setOrderId(orderId);
        sales.setUserId(userId);
        sales.setReason(reason);
        sales.setStatus(0); // 0: 待处理
        sales.setCreateTime(LocalDateTime.now()); // 补全时间
        afterSalesMapper.insert(sales);
    }

    // ==========================================
    //               管理员端功能
    // ==========================================

    public IPage<Orders> getAdminOrderList(Integer page, Integer size, Integer status, String orderNo) {
        Page<Orders> p = new Page<>(page, size);
        QueryWrapper<Orders> query = new QueryWrapper<>();

        if (status != null) {
            query.eq("status", status);
        }
        if (StringUtils.hasText(orderNo)) {
            query.like("order_no", orderNo);
        }
        query.orderByDesc("create_time");

        IPage<Orders> result = ordersMapper.selectPage(p, query);
        fillOrderItems(result.getRecords());
        return result;
    }

    public void shipOrder(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 1) throw new RuntimeException("当前状态不可发货");

        order.setStatus(2); // 2: 已发货
        order.setDeliveryTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    public void deleteOrder(Long orderId) {
        ordersMapper.deleteById(orderId);
        // 最好也删除关联的 Item，或者逻辑删除
        orderItemMapper.delete(new QueryWrapper<OrderItem>().eq("order_id", orderId));
    }

    // ==========================================
    //               辅助私有方法
    // ==========================================

    private Orders checkOwner(Long userId, Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        return order;
    }

    private void fillOrderItems(List<Orders> orders) {
        if (orders == null || orders.isEmpty()) return;
        for (Orders order : orders) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId())
            );
            order.setItems(items);
        }
    }
}