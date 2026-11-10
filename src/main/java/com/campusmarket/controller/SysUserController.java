package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.cache.CacheStore;
import com.campusmarket.common.CacheKeys;
import com.campusmarket.entity.SysUser;
import com.campusmarket.service.CaptchaService;
import com.campusmarket.service.RbacService;
import com.campusmarket.service.SysUserService;
import com.campusmarket.utils.JwtUtil;
import com.campusmarket.utils.PasswordUtil;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RbacService rbacService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private CacheStore cacheStore;

    /**
     * 是否强制校验图形验证码
     * 联调期前端还没做完验证码时可以临时关掉：auth.captcha.enabled=false
     * 生产环境必须保持 true，否则登录接口会被脚本刷
     */
    @Value("${auth.captcha.enabled:true}")
    private boolean captchaEnabled;

    // 查询全部用户接口 GET /api/v1/user/list
    @GetMapping("/list")
    public R<List<SysUser>> list() {
        List<SysUser> userList = sysUserService.list();
        // 清空密码返回前端
        userList.forEach(u -> u.setPassword(null));
        return R.ok(userList);
    }

    // 登录接口 POST /api/v1/user/login（放行）
    @PostMapping("/login")
    public R login(@RequestBody Map<String,String> param){
        // 兼容前端传 username 或 studentNo，哪个有值用哪个
        String account = param.get("username");
        if (account == null || account.isEmpty()) {
            account = param.get("studentNo");
        }
        String password = param.get("password");

        // 登录失败限流：同一账号 60 秒内错 5 次就锁窗口，防脚本撞密码
        String failKey = CacheKeys.LOGIN_FAIL + account;
        String failStr = cacheStore.get(failKey);
        int failCount = failStr == null ? 0 : Integer.parseInt(failStr);
        if (failCount >= 5) {
            return R.error(429, "登录失败次数过多，请 1 分钟后再试");
        }

        boolean smsLogin = false;
        // 验证码校验（反爬第一道闸）：一次性使用，校验完即失效
        if (captchaEnabled) {
            String captchaKey = param.get("captchaKey");
            String captchaCode = param.get("captchaCode");
            // 短信登录分支：captchaKey = sms:{手机号}，走短信验证码而不是图形验证码
            if (captchaKey != null && captchaKey.startsWith("sms:")) {
                String expected = cacheStore.get(captchaKey);
                if (expected == null || !expected.equals(captchaCode)) {
                    return R.error(400, "短信验证码错误或已过期");
                }
                cacheStore.delete(captchaKey);
                smsLogin = true;
            } else {
                if (!captchaService.verify(captchaKey, captchaCode)) {
                    return R.error(400, "验证码错误或已失效，请刷新后重试");
                }
            }
        }

        SysUser user;
        if (smsLogin) {
            // 短信登录：按手机号找账号，账号就是手机号
            user = sysUserService.lambdaQuery()
                    .eq(SysUser::getPhone, account)
                    .one();
        } else {
            // 密码登录：按学号找账号
            user = sysUserService.lambdaQuery()
                    .eq(SysUser::getStudentNo, account)
                    .one();
        }

        // 统一提示文案，避免通过错误信息判断"该学号是否已注册"（防账号枚举）
        if(user == null){
            cacheStore.set(failKey, String.valueOf(failCount + 1), 60);
            return R.fail("账号或密码错误");
        }
        // 密码登录才校验 BCrypt；短信登录凭验证码即视为本人
        if (!smsLogin && !PasswordUtil.matches(password, user.getPassword())) {
            cacheStore.set(failKey, String.valueOf(failCount + 1), 60);
            return R.fail("账号或密码错误");
        }

        // 账号状态校验：只有 NORMAL 可登录，被封禁/注销的账号即使密码正确也拒绝
        // 放在密码校验之后，避免通过错误信息探测账号状态
        if (!"NORMAL".equalsIgnoreCase(user.getAccountStatus())) {
            return R.error(403, "账号状态异常（" + user.getAccountStatus() + "），请联系管理员");
        }

        // 登录成功，清掉失败计数
        cacheStore.delete(failKey);

        // 记录最后登录时间：该字段建表时有，但此前从未写入，后台看板一直为空
        SysUser loginRecord = new SysUser();
        loginRecord.setId(user.getId());
        loginRecord.setLastLoginTime(LocalDateTime.now());
        sysUserService.updateById(loginRecord);

        String token = JwtUtil.generateToken(user.getId());
        // RBAC：登录时一并返回角色编码与权限编码，供前端判断管理员/按钮显隐/路由守卫
        Set<String> roleCodes = rbacService.getUserRoleCodes(user.getId());
        Set<String> permSet = rbacService.getUserPermCodes(user.getId());
        user.setRoleCode(roleCodes.isEmpty() ? null : roleCodes.iterator().next());
        user.setPerms(new ArrayList<>(permSet));
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        // 兼容：顶层也保留一份
        data.put("roles", roleCodes);
        data.put("perms", permSet);
        return R.ok(data);
    }

    // 注册接口 POST /api/v1/user/register（放行）
    @PostMapping("/register")
    public R register(@RequestBody SysUser sysUser){
        // 必填校验
        String studentNo = sysUser.getStudentNo();
        String phone = sysUser.getPhone();
        if (studentNo == null || studentNo.trim().isEmpty()) {
            return R.fail("学号不能为空");
        }
        if (sysUser.getPassword() == null || sysUser.getPassword().trim().isEmpty()) {
            return R.fail("密码不能为空");
        }
        // 手机号格式校验（11 位大陆手机号）
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return R.fail("请输入正确的手机号");
        }

        // 判断学号是否重复
        Long count = sysUserService.lambdaQuery()
                .eq(SysUser::getStudentNo, studentNo)
                .count();
        if(count > 0){
            return R.fail("该学号已注册");
        }
        // 判断手机号是否重复：一个手机号只能绑定一个账号
        Long phoneCount = sysUserService.lambdaQuery()
                .eq(SysUser::getPhone, phone)
                .count();
        if (phoneCount > 0) {
            return R.fail("该手机号已被注册");
        }

        // ✅ 用你 PasswordUtil 的 encrypt 加密（BCrypt：自带随机盐，同一密码不同人密文不同）
        String encodePwd = PasswordUtil.encrypt(sysUser.getPassword());
        sysUser.setPassword(encodePwd);
        sysUser.setId(null);
        boolean save = sysUserService.save(sysUser);
        if(save){
            // 注册成功后默认绑定 STUDENT 角色（RBAC 权限）
            rbacService.bindDefaultRole(sysUser.getId());
            return R.ok("注册成功");
        }else {
            return R.fail("注册失败");
        }
    }

    // 获取当前登录用户信息 GET /api/v1/user/info（需要token）
    @GetMapping("/info")
    public R<SysUser> getUserInfo(){
        Long userId = UserContext.getUserId();
        SysUser user = sysUserService.getById(userId);
        user.setPassword(null);
        return R.ok(user);
    }
}
