package com.supermarket.server.common.dto;
import lombok.Data;
import java.util.List;

@Data
public class OrderSubmitRequest {
    private List<Long> cartIds; // 购买的购物车项ID
    private Long addressId;     // 收货地址ID
    private Long couponId;      // 优惠券ID (可选)
    private Integer payType;    // 1:微信 2:支付宝 3:余额
    private String remark;      // 订单备注 (新增)
}