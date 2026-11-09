package com.campusmarket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.R;
import com.campusmarket.entity.Goods;
import com.campusmarket.service.GoodsService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 发布商品
     */
    @PostMapping("/add")
    public R add(@RequestBody Goods goods) {
        Long userId = UserContext.getUserId();
        // 设置发布人id为当前登录用户
        goods.setSellerId(userId);
        // 审核状态与商品状态必填（数据库 NOT NULL），默认待审核 + 上架
        if (goods.getAuditStatus() == null || goods.getAuditStatus().isEmpty()) {
            goods.setAuditStatus("PENDING");
        }
        if (goods.getGoodsStatus() == null || goods.getGoodsStatus().isEmpty()) {
            goods.setGoodsStatus("ON_SHELF");
        }
        boolean save = goodsService.save(goods);
        if (save) {
            return R.ok("发布成功");
        }
        return R.fail("发布失败");
    }

    /**
     * 删除商品，只能删自己的
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return R.fail("商品不存在");
        }
        if (!goods.getSellerId().equals(userId)) {
            return R.fail("无权删除别人的商品");
        }
        boolean remove = goodsService.removeById(id);
        return remove ? R.ok("删除成功") : R.fail("删除失败");
    }

    /**
     * 修改商品，只能修改自己发布的
     */
    @PutMapping("/update")
    public R update(@RequestBody Goods goods) {
        Long userId = UserContext.getUserId();
        Goods oldGoods = goodsService.getById(goods.getId());
        if (oldGoods == null) {
            return R.fail("商品不存在");
        }
        if (!oldGoods.getSellerId().equals(userId)) {
            return R.fail("无权修改别人的商品");
        }
        boolean update = goodsService.updateById(goods);
        return update ? R.ok("修改成功") : R.fail("修改失败");
    }

    /**
     * 分页查询所有商品
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    @GetMapping("/list")
    public R<IPage<Goods>> list(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) Long page,
            @RequestParam(name = "page_size", required = false) Long pageSizeSnake
    ) {
        // 兼容前端 page/page_size（下划线）与原有 pageNum/pageSize 两种分页参数
        long pn = page != null ? page : (pageNum != null ? pageNum : 1L);
        long ps = pageSizeSnake != null ? pageSizeSnake : (pageSize != null ? pageSize : 10L);
        if (pn < 1) {
            pn = 1L;
        }
        if (ps < 1) {
            ps = 10L;
        }
        Page<Goods> pageObj = new Page<>(pn, ps);
        IPage<Goods> pageData = goodsService.page(pageObj);
        return R.ok(pageData);
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public R<Goods> getDetail(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return R.fail("商品不存在");
        }
        return R.ok(goods);
    }

    /**
     * 我发布的商品
     */
    @GetMapping("/my")
    public R<IPage<Goods>> myGoods(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        Long userId = UserContext.getUserId();
        Page<Goods> page = new Page<>(pageNum, pageSize);
        IPage<Goods> pageData = goodsService.lambdaQuery()
                .eq(Goods::getSellerId, userId)
                .page(page);
        return R.ok(pageData);
    }

    /**
     * 商品上下架（仅自己发布的商品）
     *
     * PUT /api/v1/goods/{id}/status?status=ON_SHELF|OFF_SHELF
     * SOLD 状态由订单流程触发，这里不允许手动改。
     */
    @PutMapping("/{id}/status")
    public R changeStatus(@PathVariable Long id, @RequestParam String status) {
        Long userId = UserContext.getUserId();
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return R.fail("商品不存在");
        }
        if (!goods.getSellerId().equals(userId)) {
            return R.fail("无权操作别人的商品");
        }
        if (!"ON_SHELF".equals(status) && !"OFF_SHELF".equals(status)) {
            return R.error(400, "status 只能是 ON_SHELF（上架）或 OFF_SHELF（下架）");
        }
        Goods upd = new Goods();
        upd.setId(id);
        upd.setGoodsStatus(status);
        goodsService.updateById(upd);
        return R.ok("操作成功");
    }
}
