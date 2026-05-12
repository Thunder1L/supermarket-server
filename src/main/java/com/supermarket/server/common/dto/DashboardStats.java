package com.supermarket.server.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStats {
    private BigDecimal todaySales;    // 今日销售额
    private Long totalOrders;         // 总订单数
    private Long totalProducts;       // 商品总数
    private Long totalUsers;          // 用户总数

    // 图表数据 (简单起见，这里返回两个数组)
    private List<String> dateList;    //日期轴
    private List<BigDecimal> salesList; //销售额轴

    // 最新订单列表 (复用 Orders 实体或新建 DTO)
    private List<?> recentOrders;
}