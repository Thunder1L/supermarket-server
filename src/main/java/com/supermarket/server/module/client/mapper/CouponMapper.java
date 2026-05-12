package com.supermarket.server.module.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.server.module.client.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
}