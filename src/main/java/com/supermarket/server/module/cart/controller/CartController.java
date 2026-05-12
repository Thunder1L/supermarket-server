package com.supermarket.server.module.cart.controller;

import com.supermarket.server.common.dto.CartVo;
import com.supermarket.server.common.dto.OrderSubmitRequest;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.cart.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return jwtUtil.parseToken(token).get("userId", Long.class);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody CartVo req, HttpServletRequest request) {
        cartService.add(getUserId(request), req.getProductId());
        return Result.success("已加入购物车");
    }

    @GetMapping("/list")
    public Result<List<CartVo>> list(HttpServletRequest request) {
        return Result.success(cartService.list(getUserId(request)));
    }

    @PostMapping("/update/{id}/{count}")
    public Result<String> update(@PathVariable Long id, @PathVariable Integer count, HttpServletRequest request) {
        cartService.updateCount(getUserId(request), id, count);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        cartService.delete(getUserId(request), id);
        return Result.success("删除成功");
    }

    // 【修改点 1】路径改为 /checkout，与前端一致
    @PostMapping("/checkout")
    public Result<String> checkout(@RequestBody OrderSubmitRequest req, HttpServletRequest request) {
        try {
            // 【修改点 2】调用 service 的 checkout 方法
            // (请确保 CartService.java 里的方法名也是 checkout，而不是 submitOrder)
            String orderNo = cartService.checkout(getUserId(request), req);
            return Result.success(orderNo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}