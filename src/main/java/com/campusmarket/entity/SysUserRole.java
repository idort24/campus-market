package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_role")
public class SysUserRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;            // 用户ID user_id，关联 sys_user.id
    private Long roleId;            // 角色ID role_id，关联 sys_role.id
    private LocalDateTime createTime; // 创建时间 create_time

    @TableLogic
    private Integer isDeleted;      // 逻辑删除 is_deleted
}
