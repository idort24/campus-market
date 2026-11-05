package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentNo;        // 学号 student_no
    private String email;            // 邮箱
    private String password;         // 密码
    private String nickName;         // 昵称 nick_name
    private String avatar;           // 头像
    private String department;       // 院系
    private String grade;            // 年级
    private Integer creditScore;     // 信誉分 credit_score
    private String creditLevel;      // 信誉等级 credit_level（新增）
    private Integer userType;        // 用户类型 user_type：1普通学生 9管理员（改 Integer）
    private String accountStatus;    // 账号状态 account_status
    private LocalDateTime lastLoginTime; // 最后登录时间 last_login_time
    private LocalDateTime logoutTime;    // 注销时间 logout_time
    private String logoutMaskInfo;   // 注销脱敏信息 logout_mask_info（新增）
    private String extJson;          // 扩展 JSON ext_json
    private LocalDateTime createTime;    // 创建时间 create_time
    private LocalDateTime updateTime;    // 更新时间 update_time

    // 非数据库字段：登录时返回给前端的角色编码与权限编码（不落库）
    @TableField(exist = false)
    private String roleCode;

    @TableField(exist = false)
    private List<String> perms;

    @TableLogic
    private Integer isDeleted;       // 逻辑删除 is_deleted
}
