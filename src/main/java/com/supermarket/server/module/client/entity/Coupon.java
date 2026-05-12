package com.supermarket.server.module.client.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {
    private String name;
    private BigDecimal amount;
    private BigDecimal minSpend;
    private Integer type; // 1注册 2生日 3通用
}