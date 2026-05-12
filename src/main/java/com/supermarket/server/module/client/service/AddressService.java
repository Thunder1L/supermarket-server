package com.supermarket.server.module.client.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.module.client.entity.Address;
import com.supermarket.server.module.client.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    // 获取我的地址列表
    public List<Address> getMyAddresses(Long userId) {
        return addressMapper.selectList(new QueryWrapper<Address>()
                .eq("user_id", userId)
                .orderByDesc("is_default") // 默认地址排前面
                .orderByDesc("create_time"));
    }

    // 新增地址
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long userId, Address address) {
        address.setUserId(userId);

        // 1. 检查是否是该用户的第一条地址
        Long count = addressMapper.selectCount(new QueryWrapper<Address>().eq("user_id", userId));
        if (count == 0) {
            address.setIsDefault(1); // 第一条自动设为默认
        } else {
            address.setIsDefault(0);
        }

        // TODO: 如果前端传了 isDefault=1，需要把其他地址改为0（互斥逻辑），这里暂略

        addressMapper.insert(address);
    }

    // 删除地址
    public void deleteAddress(Long userId, Long addressId) {
        addressMapper.delete(new QueryWrapper<Address>()
                .eq("id", addressId)
                .eq("user_id", userId)); // 只能删除自己的
    }
}