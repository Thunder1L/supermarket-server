package com.supermarket.server.module.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.dto.CartVo;
import com.supermarket.server.common.dto.OrderSubmitRequest;
import com.supermarket.server.module.cart.entity.Cart;
import com.supermarket.server.module.cart.mapper.CartMapper;
import com.supermarket.server.module.client.entity.Address;
import com.supermarket.server.module.client.entity.Coupon;
import com.supermarket.server.module.client.entity.UserCoupon;
import com.supermarket.server.module.client.mapper.AddressMapper;
import com.supermarket.server.module.client.mapper.CouponMapper;
import com.supermarket.server.module.client.mapper.UserCouponMapper;
import com.supermarket.server.module.order.entity.OrderItem;
import com.supermarket.server.module.order.entity.Orders;
import com.supermarket.server.module.order.mapper.OrderItemMapper;
import com.supermarket.server.module.order.mapper.OrdersMapper;
import com.supermarket.server.module.product.entity.Product;
import com.supermarket.server.module.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired private CartMapper cartMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrdersMapper ordersMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private AddressMapper addressMapper;
    // 即使你现在还没建 Coupon 表，先注释掉或者留着等建表
    // @Autowired private CouponMapper couponMapper;
    // @Autowired private UserCouponMapper userCouponMapper;

    // 添加购物车
    public void add(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) throw new RuntimeException("商品已下架");
        if (product.getStock() <= 0) throw new RuntimeException("商品库存不足");

        Cart exist = cartMapper.selectOne(new QueryWrapper<Cart>()
                .eq("user_id", userId).eq("product_id", productId));

        if (exist != null) {
            exist.setCount(exist.getCount() + 1);
            cartMapper.updateById(exist);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setCount(1);
            cartMapper.insert(cart);
        }
    }

    // 获取列表
    public List<CartVo> list(Long userId) {
        List<Cart> carts = cartMapper.selectList(new QueryWrapper<Cart>().eq("user_id", userId));
        List<CartVo> voList = new ArrayList<>();
        if (carts.isEmpty()) return voList;

        List<Long> productIds = carts.stream().map(Cart::getProductId).collect(Collectors.toList());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        for (Cart c : carts) {
            Product p = productMap.get(c.getProductId());
            if (p != null) {
                CartVo vo = new CartVo();
                vo.setId(c.getId());
                vo.setProductId(p.getId());
                vo.setProductName(p.getName());
                vo.setProductImg(p.getImgUrl());
                vo.setPrice(p.getPrice());
                vo.setStock(p.getStock());
                vo.setCount(c.getCount());
                voList.add(vo);
            }
        }
        return voList;
    }

    // 更新数量
    public void updateCount(Long userId, Long cartId, Integer count) {
        if (count <= 0) {
            cartMapper.deleteById(cartId);
            return;
        }
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setCount(count);
        cartMapper.updateById(cart);
    }

    // 删除
    public void delete(Long userId, Long cartId) {
        cartMapper.deleteById(cartId);
    }

    // 【修改】方法名改为 checkout，与 Controller 保持一致
    @Transactional(rollbackFor = Exception.class)
    public String checkout(Long userId, OrderSubmitRequest req) {
        // 1. 基础校验
        List<Cart> carts = cartMapper.selectBatchIds(req.getCartIds());
        if (carts.isEmpty()) throw new RuntimeException("购物车商品无效");

        Address address = addressMapper.selectById(req.getAddressId());
        if (address == null) throw new RuntimeException("收货地址不存在");

        // 2. 计算 & 扣库存
        BigDecimal goodsTotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (Cart c : carts) {
            Product p = productMapper.selectById(c.getProductId());
            if (p.getStock() < c.getCount()) throw new RuntimeException(p.getName() + " 库存不足");

            p.setStock(p.getStock() - c.getCount());
            p.setSales(p.getSales() + c.getCount());
            productMapper.updateById(p);

            BigDecimal itemAmount = p.getPrice().multiply(new BigDecimal(c.getCount()));
            goodsTotal = goodsTotal.add(itemAmount);

            OrderItem item = new OrderItem();
            item.setProductId(p.getId());
            item.setProductName(p.getName());
            item.setProductImg(p.getImgUrl());
            item.setPrice(p.getPrice());
            item.setCount(c.getCount());
            item.setTotalPrice(itemAmount);
            items.add(item);
        }

        // 3. 优惠券逻辑 (暂时简化，如果你还没建 Coupon 表，先注释掉)
        // BigDecimal discountAmount = BigDecimal.ZERO;
        // if (req.getCouponId() != null) { ... }

        BigDecimal finalAmount = goodsTotal; // 暂无优惠

        // 4. 生成订单
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setTotalAmount(finalAmount);
        order.setStatus(1); // 1:待发货 (模拟支付成功)

        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getRegion() + " " + address.getDetailAddress());
        order.setCreateTime(LocalDateTime.now());

        ordersMapper.insert(order);

        // 5. 保存详情
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 6. 清空购物车
        cartMapper.delete(new QueryWrapper<Cart>().in("id", req.getCartIds()));

        return order.getOrderNo();
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000;
        return "DD" + time + random;
    }
}