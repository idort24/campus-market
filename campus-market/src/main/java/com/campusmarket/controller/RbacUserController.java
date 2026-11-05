package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.service.RbacService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class RbacUserController {

    @Autowired
    private RbacService rbacService;

    // 当前登录用户的角色与权限编码集合，供前端按钮显隐 GET /api/v1/user/permissions
    @GetMapping("/permissions")
    public R<Map<String, Object>> myPermissions() {
        Long userId = UserContext.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("roles", rbacService.getUserRoleCodes(userId));
        data.put("perms", rbacService.getUserPermCodes(userId));
        return R.ok(data);
    }
}
