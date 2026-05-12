package com.supermarket.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 【核心】禁用 CSRF (Spring Security 6 写法)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 启用跨域
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3.添加安全响应头设置
                .headers(headers -> headers
                        .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable) // 先禁用默认，再手动配置或者直接使用默认
                        // 其实 Spring Security 默认是开启的。
                        // 如果报错提示缺失，说明可能被某些配置覆盖了。
                        // 强制开启 X-Content-Type-Options: nosniff
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                )

                // 4. 权限控制
                .authorizeHttpRequests(auth -> auth
                        // 放行所有 /api 开头的请求 (登录、注册、验证码都在这)
                        .requestMatchers("/api/**").permitAll()

                        .requestMatchers("/images/**").permitAll()

                        // 放行 Swagger 和 静态资源
                        .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 其他接口需要认证
                        .anyRequest().authenticated()
                );


        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}