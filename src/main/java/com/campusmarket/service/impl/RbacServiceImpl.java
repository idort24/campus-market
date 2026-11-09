package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.dto.PermissionNode;
import com.campusmarket.entity.SysPermission;
import com.campusmarket.entity.SysRole;
import com.campusmarket.entity.SysRolePermission;
import com.campusmarket.entity.SysUserRole;
import com.campusmarket.mapper.SysPermissionMapper;
import com.campusmarket.mapper.SysRoleMapper;
import com.campusmarket.mapper.SysRolePermissionMapper;
import com.campusmarket.mapper.SysUserRoleMapper;
import com.campusmarket.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacServiceImpl implements RbacService {

    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysPermissionMapper permissionMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysRolePermissionMapper rolePermMapper;

    @Override
    public Page<SysRole> pageRoles(long pageNum, long pageSize, String roleName, Integer status) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> w = new LambdaQueryWrapper<>();
        w.like(roleName != null && !roleName.isEmpty(), SysRole::getRoleName, roleName)
         .eq(status != null, SysRole::getStatus, status)
         .orderByAsc(SysRole::getSort);
        return roleMapper.selectPage(page, w);
    }

    @Override
    public boolean saveRole(SysRole role) {
        if (role.getRoleCode() == null || role.getRoleCode().isEmpty()) {
            return false;
        }
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, role.getRoleCode()));
        if (count > 0) {
            return false; // 角色编码唯一
        }
        role.setId(null);
        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean updateRole(SysRole role) {
        if (role.getId() == null) {
            return false;
        }
        return roleMapper.updateById(role) > 0;
    }

    @Override
    public boolean deleteRole(Long roleId) {
        SysRole r = roleMapper.selectById(roleId);
        if (r == null) {
            return false;
        }
        // 内置角色不可删除
        String code = r.getRoleCode();
        if ("ADMIN".equalsIgnoreCase(code) || "STUDENT".equalsIgnoreCase(code)) {
            return false;
        }
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    public List<SysPermission> listPermissions(String permType) {
        LambdaQueryWrapper<SysPermission> w = new LambdaQueryWrapper<>();
        w.like(permType != null && !permType.isEmpty(), SysPermission::getPermType, permType)
         .orderByAsc(SysPermission::getSort);
        return permissionMapper.selectList(w);
    }

    @Override
    @Transactional
    public boolean assignRolePermissions(Long roleId, List<Long> permissionIds) {
        // 全量覆盖：先逻辑删除旧的关联，再插入新的
        rolePermMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        for (Long pid : permissionIds.stream().distinct().collect(Collectors.toList())) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(pid);
            rolePermMapper.insert(rp);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean assignUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        for (Long rid : roleIds.stream().distinct().collect(Collectors.toList())) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            userRoleMapper.insert(ur);
        }
        return true;
    }

    @Override
    public Set<String> getUserRoleCodes(Long userId) {
        List<SysUserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream().map(SysRole::getRoleCode).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getUserPermCodes(Long userId) {
        List<SysUserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).distinct().collect(Collectors.toList());
        List<SysRolePermission> rps = rolePermMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, roleIds));
        if (rps.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> permIds = rps.stream().map(SysRolePermission::getPermissionId)
                .distinct().collect(Collectors.toList());
        List<SysPermission> perms = permissionMapper.selectBatchIds(permIds);
        return perms.stream().map(SysPermission::getPermCode).collect(Collectors.toSet());
    }

    @Override
    public void bindDefaultRole(Long userId) {        SysRole student = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "STUDENT"));
        if (student == null) {
            return;
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, student.getId()));
        if (count == 0) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(student.getId());
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public List<SysPermission> getUserMenus(Long userId) {
        List<SysUserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).distinct().collect(Collectors.toList());
        List<SysRolePermission> rps = rolePermMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, roleIds));
        if (rps.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> permIds = rps.stream().map(SysRolePermission::getPermissionId)
                .distinct().collect(Collectors.toList());
        List<SysPermission> perms = permissionMapper.selectBatchIds(permIds);
        // 只保留菜单类权限，并按 sort 升序，前端拿到的顺序即为展示顺序
        return perms.stream()
                .filter(p -> "MENU".equalsIgnoreCase(p.getPermType()))
                .sorted((a, b) -> Integer.compare(
                        a.getSort() == null ? 0 : a.getSort(),
                        b.getSort() == null ? 0 : b.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionNode> listPermissionTree() {
        List<SysPermission> all = permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort));
        Map<Long, PermissionNode> nodeMap = new LinkedHashMap<>();
        for (SysPermission p : all) {
            PermissionNode node = new PermissionNode();
            node.setId(p.getId());
            node.setPermCode(p.getPermCode());
            node.setPermName(p.getPermName());
            node.setPermType(p.getPermType());
            node.setParentId(p.getParentId());
            node.setPermPath(p.getPermPath());
            node.setSort(p.getSort());
            node.setChildren(new ArrayList<>());
            nodeMap.put(p.getId(), node);
        }
        List<PermissionNode> roots = new ArrayList<>();
        for (PermissionNode node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                PermissionNode parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }
}
