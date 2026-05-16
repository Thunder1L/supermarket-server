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
    // 【修复 1】把 Result<String> 泛型改为 Result<Long>，因为现在返回的是订单 ID
    public Result<Long> checkout(@RequestBody OrderSubmitRequest req, HttpServletRequest request) {
        try {
            // 【修复 2】用 Long 类型的 orderId 来接收 service 返回的真实订单 ID
            Long orderId = cartService.checkout(getUserId(request), req);

            // 【修复 3】把拿到的 orderId 原封不动地通过 Result 返回给前端
            return Result.success(orderId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}