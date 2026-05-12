package com.supermarket.server.common.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartVo {
    private Long id;          // 购物车记录ID
    private Long productId;   // 商品ID
    private String productName;
    private String productImg;
    private BigDecimal price; // 单价
    private Integer count;    // 购买数量
    private Integer stock;    // 当前库存 (前端限制最大购买数)
}
