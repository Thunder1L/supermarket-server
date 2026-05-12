package com.supermarket.server.module.client.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.client.entity.BankCard;
import com.supermarket.server.module.client.mapper.BankCardMapper;
import jakarta.servlet.http.HttpServletRequest; // 注意：Spring Boot 3 必须用 jakarta
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card")
public class CardController {

    @Autowired
    private BankCardMapper bankCardMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 辅助方法：从 Token 解析用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("请先登录");
        }
        // 假设你的 JwtUtil.parseToken 返回 Claims 对象，并且里面存了 userId
        return jwtUtil.parseToken(token).get("userId", Long.class);
    }

    /**
     * 获取我的银行卡列表
     */
    @GetMapping("/list")
    public Result<List<BankCard>> list(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            List<BankCard> list = bankCardMapper.selectList(
                    new QueryWrapper<BankCard>().eq("user_id", userId).orderByDesc("create_time")
            );
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加银行卡
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody BankCard card, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            card.setUserId(userId);

            // 设置默认值
            if (card.getCardType() == null) {
                card.setCardType("储蓄卡");
            }

            bankCardMapper.insert(card);
            return Result.success("绑定成功");
        } catch (Exception e) {
            return Result.error("绑定失败: " + e.getMessage());
        }
    }

    /**
     * 解绑银行卡
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);

            // 安全检查：只能删除自己的卡
            BankCard card = bankCardMapper.selectById(id);
            if (card == null) {
                return Result.error("卡片不存在");
            }
            if (!card.getUserId().equals(userId)) {
                return Result.error("无权操作");
            }

            bankCardMapper.deleteById(id);
            return Result.success("解绑成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}