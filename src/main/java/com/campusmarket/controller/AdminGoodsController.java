package com.campusmarket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.R;
import com.campusmarket.entity.Goods;
import com.campusmarket.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端商品审核列表
 *
 * 路径挂在 /api/v1/admin/ 下，AuthInterceptor 会自动走 adminApi 分支，
 * 要求当前用户持有 user:manage / goods:audit / report:handle 等管理类权限码之一，
 * 普通学生账号访问直接 403。
 *
 * 注意：不要把这个接口挪到 /api/v1/goods/ 下（比如 /goods/admin/list），
 * 那样路径里不包含 /api/v1/admin/ 子串，会被拦截器当成普通登录接口放过。
 */
@RestController
@RequestMapping("/api/v1/admin/goods")
public class AdminGoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 后台商品审核列表（分页），看全部状态，可按审核状态筛选
     *
     * GET /api/v1/admin/goods/list?auditStatus=PENDING&page=1&pageSize=20
     */
    @GetMapping("/list")
    public R<IPage<Goods>> list(
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String goodsStatus,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long pageSize
    ) {
        Page<Goods> pageObj = new Page<>(page, pageSize);
        IPage<Goods> data = goodsService.lambdaQuery()
                .eq(auditStatus != null && !auditStatus.isEmpty(), Goods::getAuditStatus, auditStatus)
                .eq(goodsStatus != null && !goodsStatus.isEmpty(), Goods::getGoodsStatus, goodsStatus)
                .orderByDesc(Goods::getCreateTime)
                .page(pageObj);
        return R.ok(data);
    }
}
