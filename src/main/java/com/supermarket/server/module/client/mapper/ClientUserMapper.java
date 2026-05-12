package com.supermarket.server.module.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.server.module.client.entity.ClientUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClientUserMapper extends BaseMapper<ClientUser> {
}