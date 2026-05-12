package com.supermarket.server.module.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.server.module.admin.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    // MyBatis-Plus 已经帮你写好了增删改查，这里留空即可
}