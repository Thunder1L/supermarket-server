package com.supermarket.server.module.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.dto.CartVo;
import com.supermarket.server.common.dto.OrderSubmitRequest;
import com.supermarket.server.module.cart.entity.Cart;
import com.supermarket.server.module.cart.mapper.CartMapper;
import com.supermarket.server.module.client.entity.Address;
import com.supermarket.server.module.client.entity.ClientUser;
import com.supermarket.server.module.client.entity.Coupon;
import com.supermarket.server.module.client.entity.UserCoupon;
import com.supermarket.server.module.client.mapper.AddressMapper;
import com.supermarket.server.module.client.mapper.ClientUserMapper;
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
    @Autowired private CouponMapper couponMapper;
    @Autowired private UserCouponMapper userCouponMapper;
    // 🚨 新增：用于扣减余额的 Mapper
    @Autowired private ClientUserMapper clientUserMapper;

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

    // ==========================================
    // 🚨 核心改造：checkout 方法 (注意返回值改成了 Long)
    // ==========================================
    @Transactional(rollbackFor = Exception.class)
    public Long checkout(Long userId, OrderSubmitRequest req) {
        // 1. 基础校验：购物车商品
        List<Cart> carts = cartMapper.selectBatchIds(req.getCartIds());
        if (carts.isEmpty()) throw new RuntimeException("购物车商品无效");

        // 🚨 改造一：根据配送方式智能校验地址
        String receiverName = "";
        String receiverPhone = "";
        String receiverAddress = "";

        if (req.getDeliveryType() != null && req.getDeliveryType() == 2) {
            // 外送上门：严格校验收货地址
            if (req.getAddressId() == null) throw new RuntimeException("外送订单必须提供收货地址");
            Address address = addressMapper.selectById(req.getAddressId());
            if (address == null) throw new RuntimeException("收货地址不存在");

            receiverName = address.getReceiverName();
            receiverPhone = address.getPhone();
            receiverAddress = address.getRegion() + " " + address.getDetailAddress();
        } else {
            // 门店自提：直接生成占位快照，完美绕过 null 报错
            receiverName = "客户";
            receiverPhone = "到店自提";
            receiverAddress = "门店自提订单，无需配送";
        }

        // 2. 计算金额 & 扣库存
        BigDecimal goodsTotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (Cart c : carts) {
            Product p = productMapper.selectById(c.getProductId());
            if (p.getStock() < c.getCount()) throw new RuntimeException(p.getName() + " 库存不足");

            // 高并发防超卖改造
            int updatedRows = productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Product>()
                    .setSql("stock = stock - " + c.getCount() + ", sales = sales + " + c.getCount())
                    .eq("id", p.getId())
                    .ge("stock", c.getCount()));

            if (updatedRows == 0) {
                throw new RuntimeException("手慢了，" + p.getName() + " 刚刚被别人抢走了！");
            }

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

        // 3. 优惠券核销逻辑
        BigDecimal discountAmount = BigDecimal.ZERO;
        Long usedUserCouponId = null;

        if (req.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.selectOne(new QueryWrapper<UserCoupon>()
                    .eq("id", req.getCouponId())
                    .eq("user_id", userId)
                    .eq("status", 0));

            if (userCoupon == null) {
                throw new RuntimeException("优惠券不可用或已被使用");
            }

            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon != null) {
                if (goodsTotal.compareTo(coupon.getMinAmount()) >= 0) {
                    discountAmount = coupon.getValue();
                    usedUserCouponId = userCoupon.getId();
                } else {
                    throw new RuntimeException("未达到优惠券使用门槛");
                }
            }
        }

        BigDecimal finalAmount = goodsTotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 4. 生成订单
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setTotalAmount(finalAmount);
        order.setCreateTime(LocalDateTime.now());

        // 写入新增加的配送与支付快照
        order.setDeliveryType(req.getDeliveryType() != null ? req.getDeliveryType() : 1);
        order.setPayType(req.getPayMethod() != null ? req.getPayMethod() : 1);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(req.getRemark());

        // 🚨 改造二：余额并发扣减与订单状态流转
        if (req.getPayMethod() != null && req.getPayMethod() == 2) {
            // 选择余额支付，校验钱够不够
            ClientUser user = clientUserMapper.selectById(userId);
            if (user.getBalance() == null || user.getBalance().compareTo(finalAmount) < 0) {
                throw new RuntimeException("您的账户余额不足，请充值或更换支付方式");
            }

            // 乐观锁扣减余额，防并发
            int rows = clientUserMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ClientUser>()
                    .setSql("balance = balance - " + finalAmount)
                    .eq("id", userId)
                    .ge("balance", finalAmount));

            if (rows == 0) throw new RuntimeException("余额扣减失败，请重试");
            order.setStatus(1); // 扣款成功，直接流转为 1:待发货(自提)
        } else {
            order.setStatus(0); // 微信/支付宝，流转为 0:待付款，等待前端弹窗支付
        }

        ordersMapper.insert(order);

        // 5. 保存订单项明细 & 更新优惠券状态
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        if (usedUserCouponId != null) {
            UserCoupon updateUc = new UserCoupon();
            updateUc.setId(usedUserCouponId);
            updateUc.setStatus(1);
            updateUc.setUsedTime(LocalDateTime.now());
            userCouponMapper.updateById(updateUc);
        }

        // 6. 清空购物车
        cartMapper.delete(new QueryWrapper<Cart>().in("id", req.getCartIds()));

        // 🚨 改造三：返回生成的订单 ID，让前端弹窗知道去支付哪一笔订单
        return order.getId();
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000;
        return "DD" + time + random;
    }
}