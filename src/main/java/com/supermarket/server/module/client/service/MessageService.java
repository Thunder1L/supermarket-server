package com.supermarket.server.module.client.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supermarket.server.common.entity.Message;
import com.supermarket.server.module.client.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired private MessageMapper messageMapper;

    // 发送消息
    public void send(Message msg) {
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    // 获取聊天记录 (用户与管理员之间)
    public List<Message> getHistory(Long userId) {
        // 查所有：发送者是该用户 OR 接收者是该用户
        // Admin ID 固定为 1
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.and(wrapper ->
                wrapper.eq("sender_id", userId).eq("sender_role", "USER")
                        .or()
                        .eq("receiver_id", userId).eq("sender_role", "ADMIN")
        );
        query.orderByAsc("create_time");
        return messageMapper.selectList(query);
    }

    // 管理员获取列表
    public List<Message> getAllMessages() {
        // 建议加上 create_time 倒序，让最新的消息在前面
        // 并且可以限制只查最近的 500 条，防止崩溃
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        query.last("LIMIT 500");

        return messageMapper.selectList(query);
    }
}