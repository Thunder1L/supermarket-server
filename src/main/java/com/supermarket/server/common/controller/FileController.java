package com.supermarket.server.common.controller;

import com.supermarket.server.common.result.Result;
import com.supermarket.server.config.WebMvcConfig;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件为空");
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID() + suffix;

            // 保存到本地
            File dest = new File(WebMvcConfig.UPLOAD_FOLDER + newFileName);
            file.transferTo(dest);

            // 返回访问 URL (假设后端端口8080)
            return Result.success("http://localhost:8080/images/" + newFileName);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}