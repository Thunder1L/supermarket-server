package com.supermarket.server.module.client.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_card")
public class BankCard extends BaseEntity {
    private Long userId;
    private String cardNo;
    private String bankName;
    private String cardType;
    private String holderName;
}