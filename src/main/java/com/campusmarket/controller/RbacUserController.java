package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.SysPermission;
import com.campusmarket.service.RbacService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    /**
     * 当前登录用户可见的菜单 GET /api/v1/user/menus
     *
     * 前端拿到列表后动态生成路由/侧边栏：
     * - path：前端路由地址（对应 sys_permission.perm_path）
     * - permCode：权限编码
     * - title：菜单名称
     * - parentId：父级，用于组织树形结构
     */
    @GetMapping("/menus")
    public R<List<Map<String, Object>>> myMenus() {
        Long userId = UserContext.getUserId();
        List<SysPermission> menus = rbacService.getUserMenus(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysPermission m : menus) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("permCode", m.getPermCode());
            item.put("title", m.getPermName());
            item.put("path", m.getPermPath());
            item.put("parentId", m.getParentId());
            result.add(item);
        }
        return R.ok(result);
    }
}
