package com.supermarket.server.module.cart.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.server.module.cart.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}