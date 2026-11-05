package com.campusmarket.controller;

import com.campusmarket.entity.GoodsStat;
import com.campusmarket.service.GoodsStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stat")
public class GoodsStatController {

    @Autowired
    private GoodsStatService goodsStatService;

    // 测试接口：http://localhost:8080/stat/list
    @GetMapping("/list")
    public List<GoodsStat> list() {
        return goodsStatService.list();
    }
}
