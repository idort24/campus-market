package com.campusmarket.controller;

import com.campusmarket.entity.DictData;
import com.campusmarket.service.DictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dict/data")
public class DictDataController {

    @Autowired
    private DictDataService dictDataService;

    // 测试接口：http://localhost:8080/dict/data/list
    @GetMapping("/list")
    public List<DictData> list() {
        return dictDataService.list();
    }
}
