package com.supermarket.server.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间 (自动填充)
     * JsonFormat: 让前端接收到的时间是 "2023-11-30 12:00:00" 格式，而不是数组
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间 (自动填充)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 (0:未删除 1:已删除)
     * 调用 deleteById 时，MP 会自动执行 update set is_deleted=1 where id=?
     */
    @TableLogic
    private Integer isDeleted;
}