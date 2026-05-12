package com.supermarket.server.common.entity; // 放在common下，因为两端都用
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {
    private Long senderId;
    private String senderRole; // "USER" 或 "ADMIN"
    private Long receiverId;
    private String content;
    private Integer isRead;
}