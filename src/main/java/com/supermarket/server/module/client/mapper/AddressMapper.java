package com.supermarket.server.module.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.server.module.client.entity.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址 Mapper 接口
 * 对应数据库表: address
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}