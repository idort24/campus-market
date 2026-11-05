package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;        // 角色编码 role_code
    private String roleName;        // 角色名称 role_name
    private String roleDesc;        // 角色描述 role_desc
    private Integer sort;           // 排序号 sort
    private Integer status;         // 状态 status：1启用 0停用
    private String extJson;         // 扩展字段 ext_json
    private LocalDateTime createTime; // 创建时间 create_time
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableLogic
    private Integer isDeleted;      // 逻辑删除 is_deleted
}
