package com.supermarket.server.module.client.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.client.entity.Address;
import com.supermarket.server.module.client.mapper.AddressMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address") // 确保这里的路径是 /api/address
public class AddressController {

    @Autowired private AddressMapper addressMapper;
    @Autowired private JwtUtil jwtUtil;

    // 辅助方法：从 Token 获取用户 ID
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        // 解析 Token (根据你的 JwtUtil 实现调整)
        return jwtUtil.parseToken(token).get("userId", Long.class);
    }

    // 获取地址列表
    @GetMapping("/list")
    public Result<List<Address>> list(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Address> list = addressMapper.selectList(
                new QueryWrapper<Address>()
                        .eq("user_id", userId)
                        .orderByDesc("is_default") // 默认地址排前面
                        .orderByDesc("create_time")
        );
        return Result.success(list);
    }

    // 新增地址
    @PostMapping("/add")
    public Result<String> add(@RequestBody Address address, HttpServletRequest request) {
        address.setUserId(getUserId(request));

        // 如果是该用户的第一条地址，自动设为默认
        Long count = addressMapper.selectCount(new QueryWrapper<Address>().eq("user_id", address.getUserId()));
        if (count == 0) {
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        addressMapper.insert(address);
        return Result.success("添加成功");
    }

    // 删除地址
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        addressMapper.deleteById(id);
        return Result.success("删除成功");
    }
}