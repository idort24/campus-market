package com.campusmarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    // 登录方法
    SysUser login(String studentNo, String rawPassword);
    // 新增用户（自动加密密码）
    boolean saveUser(SysUser sysUser);
}
