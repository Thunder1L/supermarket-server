package com.supermarket.server.module.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supermarket.server.common.dto.ProductQueryRequest;
import com.supermarket.server.common.result.Result;
import com.supermarket.server.module.product.entity.Category;
import com.supermarket.server.module.product.entity.Product;
import com.supermarket.server.module.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 获取分类列表
    @GetMapping("/category/list")
    public Result<List<Category>> getCategoryList() {
        return Result.success(productService.getCategoryList());
    }

    // 获取商品列表 (POST传参方便扩展)
    @PostMapping("/list")
    public Result<IPage<Product>> getProductList(@RequestBody ProductQueryRequest req) {
        return Result.success(productService.getProductList(req));
    }

    // --- 分类管理接口 ---

    @PostMapping("/category/add")
    public Result<String> addCategory(@RequestBody Category category) {
        productService.addCategory(category);
        return Result.success("添加成功");
    }

    @PostMapping("/category/update")
    public Result<String> updateCategory(@RequestBody Category category) {
        productService.updateCategory(category);
        return Result.success("修改成功");
    }

    @DeleteMapping("/category/delete/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        try {
            productService.deleteCategory(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/category/status/{id}/{status}")
    public Result<String> updateCategoryStatus(@PathVariable Long id, @PathVariable Integer status) {
        productService.updateCategoryStatus(id, status);
        return Result.success("状态更新成功");
    }

    // 新增
    @PostMapping("/add")
    public Result<String> add(@RequestBody Product product) {
        productService.add(product);
        return Result.success("添加成功");
    }

    // 修改
    @PostMapping("/update")
    public Result<String> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success("修改成功");
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success("删除成功");
    }

    // 修改状态 (上架/下架)
    @PostMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        productService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}