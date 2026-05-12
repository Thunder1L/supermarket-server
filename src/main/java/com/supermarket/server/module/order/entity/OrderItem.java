package com.supermarket.server.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单快照表
 * 记录下单那一刻商品的价格和信息（防止商品后续改价影响历史订单）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    private Long orderId;    // 归属订单ID
    private Long productId;  // 商品ID
    private String productName; // 商品名称快照
    private String productImg;  // 商品图片快照
    private BigDecimal price;   // 下单时的单价
    private Integer count;      // 购买数量
    private BigDecimal totalPrice; // 小计 (price * count)
}