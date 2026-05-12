package com.supermarket.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 图片存储路径：项目根目录/uploads/
    public static final String UPLOAD_FOLDER = System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 创建上传目录
        File file = new File(UPLOAD_FOLDER);
        if (!file.exists()) file.mkdirs();

        // 映射 URL /images/** 到本地目录
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + UPLOAD_FOLDER);
    }
}