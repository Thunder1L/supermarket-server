package com.supermarket.server.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.server.module.order.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 统计今日销售额
     * 逻辑：求和 total_amount，条件是 创建时间是今天 且 订单状态不是待付款(0)和已取消(4)
     * IFNULL(..., 0) 是为了防止今天没有订单时返回 null 导致报错
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders " +
            "WHERE DATE(create_time) = CURDATE() " +
            "AND status NOT IN (0, 4)")
    BigDecimal selectTodaySales();
}