package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.SysUser;
import com.campusmarket.service.RbacService;
import com.campusmarket.service.SysUserService;
import com.campusmarket.utils.JwtUtil;
import com.campusmarket.utils.PasswordUtil;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/v1/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RbacService rbacService;

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

        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getStudentNo, account)
                .one();

        if(user == null){
            return R.fail("账号不存在");
        }
        // ✅ 用你 PasswordUtil 的 matches 校验
        if (!PasswordUtil.matches(password, user.getPassword())) {
            return R.fail("账号或密码错误");
        }

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
        // 判断学号是否重复
        Long count = sysUserService.lambdaQuery()
                .eq(SysUser::getStudentNo, sysUser.getStudentNo())
                .count();
        if(count > 0){
            return R.fail("该学号已注册");
        }
        // ✅ 用你 PasswordUtil 的 encrypt 加密
        String encodePwd = PasswordUtil.encrypt(sysUser.getPassword());
        sysUser.setPassword(encodePwd);
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
