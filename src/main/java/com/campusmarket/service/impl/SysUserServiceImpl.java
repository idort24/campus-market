package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.SysUser;
import com.campusmarket.mapper.SysUserMapper;
import com.campusmarket.service.SysUserService;
import com.campusmarket.utils.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    /**
     * 登录校验：学号登录，BCrypt校验密码
     * @param studentNo 学号
     * @param rawPassword 用户输入的明文密码
     * @return 查到的用户，密码错误/用户不存在返回null
     */
    @Override
    public SysUser login(String studentNo, String rawPassword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStudentNo, studentNo);
        SysUser user = this.getOne(wrapper);
        // 用户不存在
        if (user == null) {
            return null;
        }
        // BCrypt密文比对，不再用equals明文对比！
        boolean passOk = PasswordUtil.matches(rawPassword, user.getPassword());
        if (passOk) {
            return user;
        }
        return null;
    }

    /**
     * 新增用户，密码自动加密之后入库
     */
    @Override
    public boolean saveUser(SysUser sysUser) {
        // 加密密码
        String encryptedPwd = PasswordUtil.encrypt(sysUser.getPassword());
        sysUser.setPassword(encryptedPwd);
        return this.save(sysUser);
    }
}
