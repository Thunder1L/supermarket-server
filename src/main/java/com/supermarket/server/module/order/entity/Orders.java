package com.supermarket.server.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Orders extends BaseEntity {

    private String orderNo;     // 订单号
    private Long userId;        // 用户ID
    private BigDecimal totalAmount; // 总金额

    // 收货信息
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    /**
     * 订单状态
     * 0:待付款 1:待发货 2:已发货 3:已完成 4:已取消 5:售后中
     */
    private Integer status;

    private LocalDateTime paymentTime; // 支付时间
    private LocalDateTime deliveryTime; // 发货时间

    /**
     * 支付方式
     * 1:微信 2:支付宝 3:余额 4:银行卡
     */
    private Integer payType;

    /**
     * 订单备注
     */
    private String remark;

    // ==========================================

    // 订单详情 (不存数据库，只用于展示)
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<OrderItem> items;
}