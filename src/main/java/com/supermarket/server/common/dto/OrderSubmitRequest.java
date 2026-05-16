package com.supermarket.server.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderSubmitRequest {
    private List<Long> cartIds; // 购买的购物车项ID
    private Long addressId;     // 收货地址ID (如果是外送则有值，自提为null)
    private Long couponId;      // 优惠券ID (可选)
    private String remark;      // 订单备注

    // 🚨 修正：和前端传过来的 payMethod 字段名保持完全一致
    private Integer payMethod;  // 1:微信/支付宝 2:余额支付

    // 🚨 新增：接收前端传过来的配送方式
    private Integer deliveryType; // 1:门店自提 2:外送上门
}