package com.campusmarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.R;
import com.campusmarket.dto.PermissionNode;
import com.campusmarket.entity.SysRole;
import com.campusmarket.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AdminRoleController {

    @Autowired
    private RbacService rbacService;

    // 角色列表（分页）GET /api/v1/roles
    @GetMapping("/roles")
    public R<Page<SysRole>> pageRoles(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        return R.ok(rbacService.pageRoles(pageNum, pageSize, roleName, status));
    }

    // 新增角色 POST /api/v1/roles
    @PostMapping("/roles")
    public R addRole(@RequestBody SysRole role) {
        boolean ok = rbacService.saveRole(role);
        return ok ? R.ok("新增成功") : R.fail("角色编码已存在");
    }

    // 修改角色 PUT /api/v1/roles/{id}
    @PutMapping("/roles/{id}")
    public R updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        return rbacService.updateRole(role) ? R.ok("修改成功") : R.fail("角色不存在");
    }

    // 删除角色 DELETE /api/v1/roles/{id}
    @DeleteMapping("/roles/{id}")
    public R deleteRole(@PathVariable Long id) {
        boolean ok = rbacService.deleteRole(id);
        return ok ? R.ok("删除成功") : R.fail("内置角色不可删除或角色不存在");
    }

    // 权限树 GET /api/v1/permissions/tree
    @GetMapping("/permissions/tree")
    public R<List<PermissionNode>> permissionTree() {
        return R.ok(rbacService.listPermissionTree());
    }

    // 给角色分配权限 PUT /api/v1/roles/{id}/permissions，body: { perm_ids: [...] }
    @PutMapping("/roles/{id}/permissions")
    public R assignRolePermissions(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<Long> ids = toLongList(body.get("perm_ids"));
        rbacService.assignRolePermissions(id, ids);
        return R.ok("分配成功");
    }

    // 查询角色已分配的权限点 ID GET /api/v1/roles/{id}/permissions
    @GetMapping("/roles/{id}/permissions")
    public R<List<Long>> rolePermIds(@PathVariable("id") Long id) {
        return R.ok(rbacService.getRolePermIds(id));
    }

    // 给用户分配角色 PUT /api/v1/users/{id}/roles，body: { role_ids: [...] }
    @PutMapping("/users/{id}/roles")
    public R assignUserRoles(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<Long> ids = toLongList(body.get("role_ids"));
        rbacService.assignUserRoles(id, ids);
        return R.ok("分配成功");
    }

    private List<Long> toLongList(Object obj) {
        if (obj == null || !(obj instanceof List)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Object> raw = (List<Object>) obj;
        return raw.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
    }
}
