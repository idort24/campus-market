package com.campusmarket.controller;

import com.campusmarket.cache.CacheStore;
import com.campusmarket.common.CacheKeys;
import com.campusmarket.common.R;
import com.campusmarket.entity.SysUser;
import com.campusmarket.service.CaptchaService;
import com.campusmarket.service.SysUserService;
import com.campusmarket.utils.JwtUtil;
import com.campusmarket.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Autowired
    private SysUserService sysUserService;

    /** 与登录接口共用同一个开关，联调期可临时关闭验证码 */
    @Value("${auth.captcha.enabled:true}")
    private boolean captchaEnabled;

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

    /**
     * 忘记密码 / 重置密码 POST /api/v1/auth/password/reset
     *
     * 请求体：{ phone, captchaKey, captchaCode, newPassword }
     *
     * 说明：项目暂时没有短信通道，当前用图形验证码承担"确认是本人操作"的作用。
     * 后续接入短信服务时，只需把验证码来源从 CaptchaService 换成短信校验，
     * 参数结构保持不变，前端不需要改。
     */
    @PostMapping("/password/reset")
    public R<String> resetPassword(@RequestBody Map<String, String> param) {
        String phone = param.get("phone");
        String newPassword = param.get("newPassword");

        if (phone == null || phone.trim().isEmpty()) {
            return R.error(400, "请输入手机号");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return R.error(400, "请输入正确的手机号");
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            return R.error(400, "新密码至少 6 位");
        }
        if (captchaEnabled && !captchaService.verify(param.get("captchaKey"), param.get("captchaCode"))) {
            return R.error(400, "验证码错误或已失效，请刷新后重试");
        }

        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getPhone, phone)
                .one();
        if (user == null) {
            return R.error(400, "该手机号未注册");
        }

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(PasswordUtil.encrypt(newPassword));
        sysUserService.updateById(update);
        return R.ok("密码重置成功，请用新密码登录");
    }
}
