package com.supermarket.server.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {
    private String name;    // 分类名称 (如：饮料、零食)
    private Integer sort;   // 排序 (数字越小越靠前)
    private Integer status; // 1:启用 0:禁用
}