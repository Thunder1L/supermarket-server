package com.supermarket.server.module.client.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.client.entity.Coupon;
import com.supermarket.server.module.client.entity.UserCoupon;
import com.supermarket.server.module.client.mapper.CouponMapper;
import com.supermarket.server.module.client.mapper.UserCouponMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired private UserCouponMapper userCouponMapper;
    @Autowired private CouponMapper couponMapper;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/my")
    public Result<List<Coupon>> getMyCoupons(HttpServletRequest request) {
        Long userId = jwtUtil.parseToken(request.getHeader("Authorization")).get("userId", Long.class);

        // 1. 查关联表
        List<UserCoupon> ucs = userCouponMapper.selectList(new QueryWrapper<UserCoupon>().eq("user_id", userId).eq("status", 0));
        if (ucs.isEmpty()) return Result.success(new ArrayList<>());

        // 2. 查详情
        List<Long> ids = ucs.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        List<Coupon> coupons = couponMapper.selectBatchIds(ids);

        return Result.success(coupons);
    }
}