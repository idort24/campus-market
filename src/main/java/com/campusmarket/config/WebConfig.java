
package com.campusmarket.config;

import com.campusmarket.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/user/login",
                        "/api/v1/user/register",
                        "/api/v1/auth/captcha",
                        "/api/v1/auth/sms/code",
                        "/api/v1/auth/password/reset",
                        // 只放行公开列表；goods/{id}、goods/my、goods/status、DELETE 等都必须登录，
                        // 之前用 /goods/* 通配导致匿名能调我的发布/删改商品，已收回
                        "/api/v1/goods/list",
                        "/api/v1/category/list",
                        "/api/v1/category/children/*",
                        "/api/v1/evaluation/toUser/*",
                        "/api/v1/file/**"
                );
    }

    // 跨域配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5174"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
