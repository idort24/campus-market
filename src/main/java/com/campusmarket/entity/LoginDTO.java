package com.campusmarket.entity;

import lombok.Data;

@Data
public class LoginDTO {
    // 学号，账号
    private String studentNo;
    // 用户输入的明文密码
    private String password;
}
