package com.campusmarket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.R;
import com.campusmarket.entity.GoodsCollect;
import com.campusmarket.entity.Goods;
import com.campusmarket.service.GoodsCollectService;
import com.campusmarket.service.GoodsService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goodsCollect")
public class GoodsCollectController {

    @Autowired
    private GoodsCollectService goodsCollectService;
    @Autowired
    private GoodsService goodsService;

    /**
     * 收藏商品
     */
    @PostMapping("/add")
    public R add(@RequestParam Long goodsId) {
        Long userId = UserContext.getUserId();
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            return R.fail("商品不存在");
        }
        Long count = goodsCollectService.lambdaQuery()
                .eq(GoodsCollect::getUserId, userId)
                .eq(GoodsCollect::getGoodsId, goodsId)
                .count();
        if (count > 0) {
            return R.fail("已经收藏过该商品");
        }
        GoodsCollect collect = new GoodsCollect();
        collect.setUserId(userId);
        collect.setGoodsId(goodsId);
        goodsCollectService.save(collect);
        return R.ok("收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{goodsId}")
    public R cancel(@PathVariable Long goodsId) {
        Long userId = UserContext.getUserId();
        boolean remove = goodsCollectService.lambdaUpdate()
                .eq(GoodsCollect::getUserId, userId)
                .eq(GoodsCollect::getGoodsId, goodsId)
                .remove();
        if (remove) {
            return R.ok("取消收藏成功");
        }
        return R.fail("收藏记录不存在");
    }

    /**
     * 我的收藏列表（分页）
     */
    @GetMapping("/my")
    public R<IPage<GoodsCollect>> myCollect(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        Long userId = UserContext.getUserId();
        Page<GoodsCollect> page = new Page<>(pageNum, pageSize);
        IPage<GoodsCollect> pageData = goodsCollectService.lambdaQuery()
                .eq(GoodsCollect::getUserId, userId)
                .page(page);
        return R.ok(pageData);
    }

    /**
     * 判断是否已收藏
     */
    @GetMapping("/has/{goodsId}")
    public R<Boolean> hasCollect(@PathVariable Long goodsId) {
        Long userId = UserContext.getUserId();
        Long count = goodsCollectService.lambdaQuery()
                .eq(GoodsCollect::getUserId, userId)
                .eq(GoodsCollect::getGoodsId, goodsId)
                .count();
        return R.ok(count > 0);
    }
}
