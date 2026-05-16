package com.supermarket.server.module.employee.controller;


import com.supermarket.server.common.result.Result;
import com.supermarket.server.module.product.entity.Product;
import com.supermarket.server.module.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
        import java.util.Map;

@RestController
@RequestMapping("/api/employee/product")
public class EmployeeProductController {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 扫码查询商品信息
     */
    @GetMapping("/info")
    public Result getProductInfo(@RequestParam("id") Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            return Result.error("商品不存在或已下架");
        }
        return Result.success(product);
    }

    /**
     * 门店盘点上报最新库存
     */
    @PostMapping("/updateStock")
    public Result updateStock(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer newStock = Integer.valueOf(params.get("stock").toString());

        Product product = new Product();
        product.setId(id);
        product.setStock(newStock);

        int rows = productMapper.updateById(product);
        if (rows > 0) {
            return Result.success("库存盘点更新成功");
        }
        return Result.error("库存更新失败");
    }
}