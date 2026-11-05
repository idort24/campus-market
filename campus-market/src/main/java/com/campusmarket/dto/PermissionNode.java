package com.campusmarket.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限点树形节点，用于 /api/v1/permissions/tree
 */
@Data
public class PermissionNode {
    private Long id;
    private String permCode;
    private String permName;
    private String permType;
    private Long parentId;
    private String permPath;
    private Integer sort;
    private List<PermissionNode> children = new ArrayList<>();
}
