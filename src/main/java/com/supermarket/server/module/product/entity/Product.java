package com.supermarket.server.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private String name;        // 商品名称
    private Long categoryId;    // 分类ID
    private String categoryName;// 分类名称(冗余字段，方便查询)
    private String description; // 商品描述
    private String imgUrl;      // 商品主图 URL
    private BigDecimal price;   // 价格
    private Integer stock;      // 库存
    private Integer sales;      // 销量 (新增)
    private Integer status;     // 1:上架 0:下架
}