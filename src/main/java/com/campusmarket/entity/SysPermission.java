package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String permCode;        // 权限编码 perm_code
    private String permName;        // 权限名称 perm_name
    private String permType;        // 权限类型 perm_type：MENU/BUTTON/API
    private Long parentId;          // 父权限ID parent_id
    private String permPath;        // 前端路由或后端接口路径 perm_path
    private Integer sort;           // 排序号 sort
    private String extJson;         // 扩展字段 ext_json
    private LocalDateTime createTime; // 创建时间 create_time
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableLogic
    private Integer isDeleted;      // 逻辑删除 is_deleted
}
