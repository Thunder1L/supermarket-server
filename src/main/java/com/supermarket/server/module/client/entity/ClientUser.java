package com.supermarket.server.module.client.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("client_user")
public class ClientUser extends BaseEntity {
    private String username;
    private String phone;
    private String password;
    private String avatar;
    private Integer status;
    private BigDecimal balance;
    private LocalDate birthday;
    private String memberNo;
    private Integer points;
}