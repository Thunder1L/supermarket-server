package com.supermarket.server.module.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_admin")
public class Admin extends BaseEntity {
    private String username;
    private String phone;
    private String password;
    private String avatar;
    private Integer role; // 1:超级管理员
}