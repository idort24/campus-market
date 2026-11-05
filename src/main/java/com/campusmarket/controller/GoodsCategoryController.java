package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.GoodsCategory;
import com.campusmarket.service.GoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    /**
     * 查询全部分类
     */
    @GetMapping("/list")
    public R<List<GoodsCategory>> list() {
        List<GoodsCategory> list = goodsCategoryService.lambdaQuery()
                .orderByAsc(GoodsCategory::getSort)
                .list();
        return R.ok(list);
    }

    /**
     * 根据父分类id查子分类
     */
    @GetMapping("/children/{parentId}")
    public R<List<GoodsCategory>> children(@PathVariable Long parentId) {
        List<GoodsCategory> list = goodsCategoryService.lambdaQuery()
                .eq(GoodsCategory::getParentId, parentId)
                .orderByAsc(GoodsCategory::getSort)
                .list();
        return R.ok(list);
    }
}
