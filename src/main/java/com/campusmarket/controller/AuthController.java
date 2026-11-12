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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
     * 开发期模拟短信通道总开关 auth.sms.dev-echo，默认 false。
     * 关闭时：响应不带 devCode（只打日志），且 /user/login 的短信分支直接拒绝；
     * 打开时：devCode 回传、短信登录放行。自动化测试 / 现场演示临时开，生产关闭。
     */
    @Value("${auth.sms.dev-echo:false}")
    private boolean smsDevEcho;

    /** 邮件发送器：未配置发件邮箱/授权码时注入为空，发码兜底走控制台日志，不影响启动 */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    /** 邮箱验证码是否在响应体回显 devCode（联调/演示用），默认 false */
    @Value("${auth.email.dev-echo:false}")
    private boolean emailDevEcho;

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
     * 开发期模拟短信通道 POST /api/v1/auth/sms/code
     *
     * 请求体 { phone }，返回 { captchaKey, expireIn, devCode? }。
     * 开发期没有真实短信网关，验证码只打在控制台；只有 auth.sms.dev-echo=true 时，
     * 才把 devCode 明文放进响应体供联调/演示读取，默认关闭。
     * 缓存 5 分钟，一次性使用。
     */
    @PostMapping("/sms/code")
    public R<Map<String, Object>> smsCode(@RequestBody Map<String, String> param) {
        String phone = param.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return R.error(400, "请输入正确的手机号");
        }
        // 6 位随机数字，前导 0 补全
        String code = String.format("%06d", new java.util.Random().nextInt(1_000_000));
        String key = "sms:" + phone;
        cacheStore.set(key, code, 300);
        System.out.println("[SMS-CODE] 开发期短信验证码 phone=" + phone + " code=" + code);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("captchaKey", key);
        data.put("expireIn", 300);
        if (smsDevEcho) {
            data.put("devCode", code);
        }
        return R.ok(data);
    }

    /**
     * 邮箱验证码 POST /api/v1/auth/email/code
     *
     * 请求体 { email }，返回 { captchaKey, expireIn, devCode? }。
     * 走免费 SMTP（QQ 邮箱授权码）真实发信；授权码未配置或发送失败时，
     * 验证码只打在控制台兜底，不阻断本地联调。缓存 5 分钟，一次性使用。
     */
    @PostMapping("/email/code")
    public R<Map<String, Object>> emailCode(@RequestBody Map<String, String> param) {
        String email = param.get("email");
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return R.error(400, "请输入正确的邮箱");
        }
        String code = String.format("%06d", new java.util.Random().nextInt(1_000_000));
        String key = CacheKeys.EMAIL_CODE + email;
        cacheStore.set(key, code, 300);

        boolean sent = false;
        if (mailSender != null && mailFrom != null && !mailFrom.trim().isEmpty()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(mailFrom);
                message.setTo(email);
                message.setSubject("校园集市 · 邮箱验证码");
                message.setText("您正在登录校园集市，验证码：" + code + "，5 分钟内有效，请勿泄露给他人。");
                mailSender.send(message);
                sent = true;
            } catch (Exception e) {
                System.out.println("[EMAIL-CODE] 邮件发送失败 email=" + email + " err=" + e.getMessage());
            }
        }
        if (!sent) {
            System.out.println("[EMAIL-CODE] 开发期邮箱验证码 email=" + email + " code=" + code);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("captchaKey", key);
        data.put("expireIn", 300);
        if (emailDevEcho) {
            data.put("devCode", code);
        }
        return R.ok(data);
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
