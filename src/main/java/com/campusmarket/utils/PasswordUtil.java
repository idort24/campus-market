package com.campusmarket.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    // 加密
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    // 校验：rawPassword 用户输入明文，hashedPassword数据库里的密文
    public static boolean matches(String rawPassword, String hashedPassword) {
        return ENCODER.matches(rawPassword, hashedPassword);
    }
}
