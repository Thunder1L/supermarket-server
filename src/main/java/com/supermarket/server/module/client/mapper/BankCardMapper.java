package com.supermarket.server.module.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.server.module.client.entity.BankCard;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankCardMapper extends BaseMapper<BankCard> {
    // 继承 BaseMapper 后，MyBatis-Plus 会自动生成增删改查 SQL
}