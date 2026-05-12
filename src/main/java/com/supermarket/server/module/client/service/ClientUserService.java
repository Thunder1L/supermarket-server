package com.supermarket.server.module.client.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.dto.*;
import com.supermarket.server.common.util.JwtUtil;
import com.supermarket.server.module.client.entity.ClientUser;
import com.supermarket.server.module.client.mapper.ClientUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class ClientUserService {

    @Autowired
    private ClientUserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 普通用户发送验证码
     */
    public void sendCode(String phone) {
        // 1. 生成 6 位随机数
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

        // 2. 打印到控制台
        System.out.println("================");
        System.out.println("【普通用户】手机号: " + phone);
        System.out.println("【验证码】: " + code);
        System.out.println("================");

        // 3. 存入 Redis
        // key 格式: "verify:user:phone"
        redisTemplate.opsForValue().set("verify:code:" + phone, code, 5, java.util.concurrent.TimeUnit.MINUTES);
    }

    // ... 同样，记得在 login, register, resetPassword 中读取 Redis 时 ...
    // key 也要改为 "verify:user:" + phone

    // 注册
    public void register(RegisterRequest req) {
        // 校验验证码
        String cacheCode = redisTemplate.opsForValue().get("verify:code:" + req.getPhone());
        if (cacheCode == null || !cacheCode.equals(req.getCode())) {
            throw new RuntimeException("验证码错误或已失效");
        }
        // 查重
        Long count = userMapper.selectCount(new QueryWrapper<ClientUser>().eq("phone", req.getPhone()));
        if (count > 0) throw new RuntimeException("该手机号已注册");

        ClientUser user = new ClientUser();
        user.setUsername(req.getUsername());
        user.setPhone(req.getPhone());
        user.setPassword(req.getPassword()); // 建议加密
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(1);
        userMapper.insert(user);
    }

    // 登录
    public LoginResult login(LoginRequest req) {
        // 1. 先查用户是否存在
        ClientUser user = userMapper.selectOne(new QueryWrapper<ClientUser>().eq("phone", req.getPhone()));
        if (user == null) throw new RuntimeException("用户不存在，请先注册");
        if (user.getStatus() != null && user.getStatus() == 0) throw new RuntimeException("账号已被封禁");

        // 2. 判断登录方式
        if ("code".equals(req.getLoginType())) {
            // === 验证码登录 ===
            String redisKey = "verify:code:" + req.getPhone();
            String cacheCode = redisTemplate.opsForValue().get(redisKey);

            // 调试日志 (排错用)
            System.out.println("--- 登录校验 ---");
            System.out.println("Redis Key: " + redisKey);
            System.out.println("Redis Value: " + cacheCode);
            System.out.println("User Input: " + req.getCode());

            // 【核心校验逻辑】
            // 1. 验证码过期 (null) -> 报错
            // 2. 验证码不匹配 (!equals) -> 报错
            if (cacheCode == null) {
                throw new RuntimeException("验证码已过期，请重新获取");
            }
            if (!cacheCode.equals(req.getCode())) {
                throw new RuntimeException("验证码错误");
            }

            // 验证通过后，建议删除验证码，防止二次使用
            redisTemplate.delete(redisKey);

        } else {
            // === 密码登录 ===
            // 如果没传 loginType 或者传了 password，走这里
            if (!req.getPassword().equals(user.getPassword())) {
                throw new RuntimeException("密码错误");
            }
        }

        // 3. 生成 Token
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), "USER");

        // 构建返回对象
        return new LoginResult()
                .setToken(token)
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setAvatar(user.getAvatar()) // 数据库里没头像就是 null，前端会处理
                .setRole("USER");
    }

    // 重置密码
    public void resetPassword(ResetPasswordRequest req) {
        String cacheCode = redisTemplate.opsForValue().get("verify:code:" + req.getPhone());
        if (cacheCode == null || !cacheCode.equals(req.getCode())) throw new RuntimeException("验证码错误");

        ClientUser user = userMapper.selectOne(new QueryWrapper<ClientUser>().eq("phone", req.getPhone()));
        if (user == null) throw new RuntimeException("用户不存在");

        user.setPassword(req.getNewPassword());
        userMapper.updateById(user);
    }

    // 获取用户详情
    public ClientUser getUserInfo(Long userId) {
        ClientUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 抹除密码
        }
        return user;
    }

    // 支持更新生日
    public LoginResult updateInfo(UpdateUserRequest req) {
        // 1. 查询当前用户
        ClientUser user = userMapper.selectById(req.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 更新用户名 (如果不为空)
        if (req.getUsername() != null && !req.getUsername().isEmpty()) {
            user.setUsername(req.getUsername());
        }

        // 3. 更新头像 (如果不为空)
        if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
            user.setAvatar(req.getAvatar());
        }

        // 4. 更新生日
        if (req.getBirthday() != null) {
            user.setBirthday(req.getBirthday());
        }

        // 5. 更新手机号
        if (req.getPhone() != null && !req.getPhone().isEmpty()) {
            user.setPhone(req.getPhone());
        }

        // 6. 执行数据库更新
        userMapper.updateById(user);

        // 7. 返回最新的用户信息给前端 (用于更新 Navbar 头像和名字)
        return new LoginResult()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setRole("USER");
        // 注意：通常修改资料不重新颁发 Token，前端继续用旧的即可
    }

    // 充值余额
//    public void recharge(Long userId, java.math.BigDecimal amount) {
//        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
//            throw new RuntimeException("充值金额必须大于0");
//        }
//        ClientUser user = userMapper.selectById(userId);
//        // 原有余额 + 充值金额
//        user.setBalance(user.getBalance().add(amount));
//        userMapper.updateById(user);
//    }

    // 充值业务逻辑
    public BigDecimal topUp(Long userId, BigDecimal amount) {
        ClientUser user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        // 原余额 + 充值金额
        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = current.add(amount);

        user.setBalance(newBalance);
        userMapper.updateById(user);

        return newBalance;
    }

    // 提现
    public java.math.BigDecimal withdraw(Long userId, java.math.BigDecimal amount) {
        ClientUser user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        java.math.BigDecimal current = user.getBalance() == null ? java.math.BigDecimal.ZERO : user.getBalance();

        if (current.compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(current.subtract(amount));
        userMapper.updateById(user);
        return user.getBalance();
    }

    // 激活会员
    public String activateMember(Long userId) {
        // 1. 先查询用户
        ClientUser user = userMapper.selectById(userId);

        //必须判空，防止 userId 错误导致空指针异常
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 检查是否已经是会员,用 StringUtils.hasText() 判断是否为空或空字符串
        if (StringUtils.hasText(user.getMemberNo())) {
            throw new RuntimeException("您已经是尊贵的会员了，无需重复激活");
        }

        //生成会员卡号逻辑补全
        // 格式：M + 时间戳后8位 + 3位随机数 (确保唯一性)
        String timeStr = String.valueOf(System.currentTimeMillis());
        // 取时间戳最后8位，防止卡号太长
        String subTime = timeStr.substring(timeStr.length() - 8);
        // 生成 100-999 的随机数
        int random = (int) ((Math.random() * 900) + 100);

        String memberNo = "M" + subTime + random;

        // 3. 更新信息
        user.setMemberNo(memberNo);
        user.setPoints(100); // 赠送100积分

        // 4. 保存到数据库
        userMapper.updateById(user);

        return memberNo;
    }

}