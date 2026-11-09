package com.campusmarket.controller;

import com.campusmarket.cache.CacheStore;
import com.campusmarket.common.CacheKeys;
import com.campusmarket.common.R;
import com.campusmarket.service.CaptchaService;
import com.campusmarket.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证相关接口：验证码、退出登录
 *
 * 统一返回体 {code,msg,data}，成功码 1。
 * 本控制器路径不参与登录拦截（已在 WebConfig 放行 /api/v1/auth/captcha），
 * 退出登录需要带有效 token 才能解析出剩余有效期。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private CacheStore cacheStore;

    /**
     * 获取图形验证码 POST /api/v1/auth/captcha
     *
     * 返回 { captchaKey, imageBase64, expireIn }
     */
    @PostMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        return R.ok(captchaService.create());
    }

    /**
     * 退出登录 POST /api/v1/auth/logout
     *
     * JWT 是无状态的，仅删除前端本地 token 并不能让原 token 失效，
     * 因此退出时把 token 写入黑名单，拦截器每次请求先查黑名单。
     * TTL 取 token 剩余有效期：token 到期后条目自然消失，不需要定时清理。
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return R.error(401, "登录失效，请重新登录");
        }
        String token = authHeader.substring(7);
        if (JwtUtil.isTokenInvalid(token)) {
            return R.error(401, "登录失效，请重新登录");
        }
        long remainSeconds = JwtUtil.getRemainingSeconds(token);
        if (remainSeconds > 0) {
            cacheStore.set(CacheKeys.TOKEN_BLACK + token, String.valueOf(System.currentTimeMillis()), remainSeconds);
        }
        return R.ok("已退出登录");
    }
}
