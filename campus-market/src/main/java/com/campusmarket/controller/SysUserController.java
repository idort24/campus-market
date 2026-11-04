package com.campusmarket.controller;

import com.campusmarket.entity.SysUser;
import com.campusmarket.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    // 测试查询全部用户接口：http://localhost:8080/user/list
    @GetMapping("/list")
    public List<SysUser> list() {
        return sysUserService.list();
    }
}