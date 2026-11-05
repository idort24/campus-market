package com.campusmarket.controller;

import com.campusmarket.entity.DemoFileBlob;
import com.campusmarket.service.DemoFileBlobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/file")
public class DemoFileBlobController {

    @Autowired
    private DemoFileBlobService demoFileBlobService;

    // 测试接口：http://localhost:8080/file/list
    @GetMapping("/list")
    public List<DemoFileBlob> list() {
        return demoFileBlobService.list();
    }
}
