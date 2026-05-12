package com.supermarket.server.module.client.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("after_sales")
public class AfterSales extends BaseEntity {
    private Long orderId;
    private Long userId;
    private String reason;
    private Integer status; // 0待处理
    private String reply;
}