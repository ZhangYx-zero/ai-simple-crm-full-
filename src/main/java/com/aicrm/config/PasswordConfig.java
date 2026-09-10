package com.aicrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 只用 Spring Security 的 BCrypt 做密码加密（不引入整套 Security）。
 * 面试点：密码为什么不能明文存？BCrypt 每次加密 salt 随机，无法反查。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
