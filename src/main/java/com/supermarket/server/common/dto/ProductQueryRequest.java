package com.supermarket.server.common.dto;
import lombok.Data;

@Data
public class ProductQueryRequest {
    private Integer page = 1;     // 当前页
    private Integer size = 10;    // 每页条数
    private Long categoryId;      // 分类ID (可选)
    private String keyword;       // 搜索关键词 (可选)
}