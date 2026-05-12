package com.supermarket.server.module.client.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
public class UserCoupon extends BaseEntity {
    private Long userId;
    private Long couponId;
    private Integer status; // 0未使用 1已使用 2过期
    private LocalDateTime getTime;
}