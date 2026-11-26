package com.campusmarket.controller;

import com.campusmarket.service.DemoFileBlobService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 历史演示用文件 Blob 表。
 *
 * F4 安全加固：原 GET /file/list 为匿名可访问的演示残留接口，已下线，
 * 不再对外暴露 HTTP 路由；实体 / Mapper / Service 保留以便历史数据查询，
 * 后续版本可整体移除。
 */
// @RestController
// @RequestMapping("/file")
public class DemoFileBlobController {

    @Autowired
    private DemoFileBlobService demoFileBlobService;
}
