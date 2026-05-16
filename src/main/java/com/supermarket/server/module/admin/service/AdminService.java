package com.supermarket.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.dto.*;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.admin.entity.Admin;
import com.supermarket.server.module.admin.mapper.AdminMapper;
import com.supermarket.server.module.client.mapper.ClientUserMapper;
import com.supermarket.server.module.order.entity.Orders;
import com.supermarket.server.module.order.mapper.OrdersMapper;
import com.supermarket.server.module.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ClientUserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // 读取配置文件中的管理员口令
    @Value("${app.admin-secret}")
    private String adminSecretKey;

    /**
     * 发送验证码
     */
    public void sendCode(String phone) {
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

        System.out.println("================");
        System.out.println("【管理员】手机号: " + phone);
        System.out.println("【验证码】: " + code);
        System.out.println("================");

        // 【修改】Key 改为 verify:admin: 避免与用户端冲突
        String key = "verify:admin:" + phone;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
    }

    /**
     * 注册
     */
//    public void register(RegisterRequest req) {
//        // 1. 校验口令
//        if (!adminSecretKey.equals(req.getAdminSecret())) {
//            throw new RuntimeException("管理员口令错误，无法注册");
//        }
//
//        // 2. 校验验证码
//        String key = "verify:admin:" + req.getPhone();
//        String cacheCode = redisTemplate.opsForValue().get(key);
//        if (cacheCode == null || !cacheCode.equals(req.getCode())) {
//            throw new RuntimeException("验证码错误或已失效");
//        }
//
//        // 3. 查重
//        Long count = adminMapper.selectCount(new QueryWrapper<Admin>().eq("phone", req.getPhone()));
//        if (count > 0) {
//            throw new RuntimeException("该手机号已注册");
//        }
//
//        // 4. 入库
//        Admin admin = new Admin();
//        admin.setUsername(req.getUsername());
//        admin.setPhone(req.getPhone());
//        admin.setPassword(req.getPassword());
//        admin.setRole(1);
//        // createTime 由 MybatisPlusConfig 自动填充，这里可以不写，或者手动写也行
//        adminMapper.insert(admin);
//
//        // 注册成功后删除验证码
//        redisTemplate.delete(key);
//    }

    public void register(RegisterRequest req) {
        // 1. 根据角色判断校验逻辑
        if ("ADMIN".equals(req.getRole())) {
            // 管理员必须校验配置文件中的超级口令
            if (!adminSecretKey.equals(req.getAdminSecret())) {
                throw new RuntimeException("管理员口令错误，无法注册");
            }
        } else if ("EMPLOYEE".equals(req.getRole())) {
            // 员工校验门店授权码 (这里暂时写死为 "STORE888"，你可以后续放在数据库或配置中)
            if (!"STORE888".equals(req.getStoreCode())) {
                throw new RuntimeException("门店授权码错误，无法注册");
            }
        } else {
            throw new RuntimeException("非法的注册角色");
        }

        // 2. 校验短信验证码
        String key = "verify:admin:" + req.getPhone();
        String cacheCode = redisTemplate.opsForValue().get(key);
        if (cacheCode == null || !cacheCode.equals(req.getCode())) {
            throw new RuntimeException("验证码错误或已失效");
        }

        // 3. 查重
        Long count = adminMapper.selectCount(new QueryWrapper<Admin>().eq("phone", req.getPhone()));
        if (count > 0) {
            throw new RuntimeException("该手机号已注册");
        }

        // 4. 入库
        Admin admin = new Admin();
        admin.setUsername(req.getUsername());
        admin.setPhone(req.getPhone());
        admin.setPassword(req.getPassword());

        // 【关键修复】根据前端传来的 role 设置数据库对应字段
        // 假设 1 代表管理员，2 代表普通员工
        admin.setRole("ADMIN".equals(req.getRole()) ? 1 : 2);

        adminMapper.insert(admin);

        // 5. 注册成功后删除验证码
        redisTemplate.delete(key);
    }

    /**
     * 登录
     */
    public LoginResult login(LoginRequest req) {
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("phone", req.getPhone()));
        if (admin == null) {
            throw new RuntimeException("账号不存在");
        }

        if ("code".equals(req.getLoginType())) {
            // 验证码登录
            String key = "verify:admin:" + req.getPhone();
            String cacheCode = redisTemplate.opsForValue().get(key);
            if (cacheCode == null || !cacheCode.equals(req.getCode())) {
                throw new RuntimeException("验证码错误");
            }
            redisTemplate.delete(key); // 登录成功删除验证码
        } else {
            // 密码登录
            if (!req.getPassword().equals(admin.getPassword())) {
                throw new RuntimeException("密码错误");
            }
        }

        // 🚨 核心修复 1：把数据库里的真实角色取出来，转成字符串类型以适配后续传参
        String realRole = String.valueOf(admin.getRole());

        // 🚨 核心修复 2：Token 里也不要写死 "ADMIN" 了，塞入真实角色
        String token = jwtUtil.createToken(admin.getId(), admin.getUsername(), realRole);

        return new LoginResult()
                .setToken(token)
                .setId(admin.getId())
                .setUsername(admin.getUsername())
                .setAvatar(admin.getAvatar())
                // 🚨 核心修复 3：把写死的 "ADMIN" 换成从数据库动态获取的 role
                .setRole(realRole); // 注意：如果你的 LoginResult 中 setRole 方法接收的是 Integer 类型，请直接传入 admin.getRole()

//        String token = jwtUtil.createToken(admin.getId(), admin.getUsername(), "ADMIN");
//
//        return new LoginResult()
//                .setToken(token)
//                .setId(admin.getId())
//                .setUsername(admin.getUsername())
//                .setAvatar(admin.getAvatar())
//                .setRole("ADMIN");
    }

    /**
     * 重置密码 (忘记密码)
     */
    public void resetPassword(ResetPasswordRequest req) {
        if (!adminSecretKey.equals(req.getAdminSecret())) {
            throw new RuntimeException("管理员口令错误");
        }

        String key = "verify:admin:" + req.getPhone();
        String cacheCode = redisTemplate.opsForValue().get(key);
        if (cacheCode == null || !cacheCode.equals(req.getCode())) {
            throw new RuntimeException("验证码错误");
        }

        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("phone", req.getPhone()));
        if (admin == null) throw new RuntimeException("管理员不存在");

        admin.setPassword(req.getNewPassword());
        adminMapper.updateById(admin);
        redisTemplate.delete(key);
    }

    // ==========================================
    // 【新增】以下两个方法用于 AdminProfile 页面
    // ==========================================

    /**
     * 更新基本信息 (头像、昵称)
     */
    public LoginResult updateInfo(UpdateUserRequest req) {
        Admin admin = adminMapper.selectById(req.getId());
        if (admin == null) throw new RuntimeException("管理员不存在");

        if (req.getUsername() != null) admin.setUsername(req.getUsername());
        if (req.getAvatar() != null) admin.setAvatar(req.getAvatar());

        adminMapper.updateById(admin);

        // 返回最新信息供前端更新缓存
        return new LoginResult()
                .setId(admin.getId())
                .setUsername(admin.getUsername())
                .setPhone(admin.getPhone())
                .setAvatar(admin.getAvatar())
                .setRole("ADMIN");
    }

    /**
     * 修改密码 (登录后在个人中心修改)
     */
    public void changePassword(ChangePasswordRequest req) {
        Admin admin = adminMapper.selectById(req.getUserId());
        if (admin == null) throw new RuntimeException("管理员不存在");

        // 校验旧密码
        if (!admin.getPassword().equals(req.getOldPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        admin.setPassword(req.getNewPassword());
        adminMapper.updateById(admin);
    }

    // ==========================================

    /**
     * 获取首页统计数据
     */
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        // 1. 基础数量
        stats.setTotalUsers(userMapper.selectCount(null));
        stats.setTotalProducts(productMapper.selectCount(null));
        stats.setTotalOrders(ordersMapper.selectCount(null));

        // 2. 今日销售额
        stats.setTodaySales(ordersMapper.selectTodaySales());

        // 3. 最新订单
        QueryWrapper<Orders> query = new QueryWrapper<>();
        query.orderByDesc("create_time").last("LIMIT 5");
        stats.setRecentOrders(ordersMapper.selectList(query));

        // 4. 模拟图表数据
        List<String> dateList = new ArrayList<>();
        List<BigDecimal> salesList = new ArrayList<>();

        // 生成过去 7 天的日期和模拟数据
        for (int i = 6; i >= 0; i--) {
            // 生成日期：今天 - i 天
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
            dateList.add(dateStr);

            // 模拟数据 (这里还是假数据，真实项目需要按日期 Group By 查询)
            // 生成一个 100~1000 的随机金额
            salesList.add(new BigDecimal((int)(Math.random() * 900) + 100));
        }

        stats.setDateList(dateList);
        stats.setSalesList(salesList);

        return stats;
    }
}