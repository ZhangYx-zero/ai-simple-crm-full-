package com.aicrm.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置。
 * 作用：拦截 /api/** 下所有请求要求登录；只有 /api/auth/login 放行。
 * 说明：这里同时启用 Sa-Token 的「注解鉴权」（@SaCheckLogin/@SaCheckRole），
 *       所以 Controller 里写 @SaCheckLogin 也会生效。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");   // 登录接口公开
    }
}
