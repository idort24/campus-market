package com.campusmarket.controller;

import com.campusmarket.entity.DictType;
import com.campusmarket.service.DictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dict/type")
public class DictTypeController {

    @Autowired
    private DictTypeService dictTypeService;

    // 测试接口：http://localhost:8080/dict/type/list
    @GetMapping("/list")
    public List<DictType> list() {
        return dictTypeService.list();
    }
}
