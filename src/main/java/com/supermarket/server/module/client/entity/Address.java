package com.supermarket.server.module.client.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("address")
public class Address extends BaseEntity {
    private Long userId;
    private String receiverName;
    private String phone;
    private String region;       // 省市区
    private String detailAddress;// 详细地址
    private Integer isDefault;   // 1默认
}