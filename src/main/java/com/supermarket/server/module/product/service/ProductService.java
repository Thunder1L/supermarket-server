package com.supermarket.server.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.server.common.dto.ProductQueryRequest;
import com.supermarket.server.module.product.entity.Category;
import com.supermarket.server.module.product.entity.Product;
import com.supermarket.server.module.product.mapper.CategoryMapper;
import com.supermarket.server.module.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    // 获取所有可用分类
    public List<Category> getCategoryList() {
        return categoryMapper.selectList(new QueryWrapper<Category>()
                .eq("status", 1)
                .orderByAsc("sort"));
    }

    // 分页搜索商品
    public IPage<Product> getProductList(ProductQueryRequest req) {
        Page<Product> page = new Page<>(req.getPage(), req.getSize());

        QueryWrapper<Product> query = new QueryWrapper<>();
        query.eq("status", 1); // 只查上架的

        // 动态条件
        if (req.getCategoryId() != null && req.getCategoryId() > 0) {
            query.eq("category_id", req.getCategoryId());
        }
        if (StringUtils.hasText(req.getKeyword())) {
            query.like("name", req.getKeyword());
        }

        query.orderByDesc("create_time");

        return productMapper.selectPage(page, query);
    }

    // 新增分类
    public void addCategory(Category category) {
        // 设默认值
        if (category.getSort() == null) category.setSort(0);
        if (category.getStatus() == null) category.setStatus(1);
        categoryMapper.insert(category);
    }

    // 修改分类
    public void updateCategory(Category category) {
        // 如果修改了分类名称，商品表里的冗余字段也要改 (这是一个数据一致性问题，简单处理可以同步更新)
        // 实际开发中可能用 MQ 或 定时任务，这里简单直接更新
        if (category.getName() != null) {
            // update product set category_name = ? where category_id = ?
            // 这里暂略，仅更新分类表
        }
        categoryMapper.updateById(category);
    }

    // 删除分类
    public void deleteCategory(Long id) {
        // 检查该分类下是否有商品，如果有，不允许删除
        Long count = productMapper.selectCount(new QueryWrapper<Product>().eq("category_id", id));
        if (count > 0) {
            throw new RuntimeException("该分类下还有商品，禁止删除");
        }
        categoryMapper.deleteById(id);
    }

    // 修改分类状态
    public void updateCategoryStatus(Long id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryMapper.updateById(category);
    }

    // 新增商品
    public void add(Product product) {
        // 补全分类名称 (根据ID查名字，存入冗余字段)
        if (product.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(product.getCategoryId());
            if (cat != null) product.setCategoryName(cat.getName());
        }
        productMapper.insert(product);
    }

    // 修改商品
    public void update(Product product) {
        // 如果修改了分类ID，也要同步修改分类名称
        if (product.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(product.getCategoryId());
            if (cat != null) product.setCategoryName(cat.getName());
        }
        productMapper.updateById(product);
    }

    // 删除商品
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    // 更新上下架状态
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.updateById(product);
    }
}