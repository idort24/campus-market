package com.campusmarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.dto.PermissionNode;
import com.campusmarket.entity.SysPermission;
import com.campusmarket.entity.SysRole;

import java.util.List;
import java.util.Set;

public interface RbacService {

    // 角色分页
    Page<SysRole> pageRoles(long pageNum, long pageSize, String roleName, Integer status);

    // 新增角色（role_code 唯一，重复返回 false）
    boolean saveRole(SysRole role);

    // 修改角色
    boolean updateRole(SysRole role);

    // 删除角色（内置 ADMIN/STUDENT 不可删）
    boolean deleteRole(Long roleId);

    // 权限点列表
    List<SysPermission> listPermissions(String permType);

    // 给角色分配权限（全量覆盖）
    boolean assignRolePermissions(Long roleId, List<Long> permissionIds);

    // 给用户分配角色（全量覆盖）
    boolean assignUserRoles(Long userId, List<Long> roleIds);

    // 查询用户拥有的角色编码集合
    Set<String> getUserRoleCodes(Long userId);

    // 查询用户拥有的权限编码集合
    Set<String> getUserPermCodes(Long userId);

    // 注册成功后绑定默认角色（STUDENT）
    void bindDefaultRole(Long userId);

    // 权限点树形列表（按 parentId 组织 children）
    List<PermissionNode> listPermissionTree();
}
