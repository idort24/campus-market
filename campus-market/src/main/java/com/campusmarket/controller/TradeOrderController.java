package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.Goods;
import com.campusmarket.entity.TradeOrder;
import com.campusmarket.service.GoodsService;
import com.campusmarket.service.TradeOrderService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
public class TradeOrderController {

    @Autowired
    private TradeOrderService tradeOrderService;
    @Autowired
    private GoodsService goodsService;

    /**
     * 下单（买家发起）
     */
    @PostMapping("/create")
    public R create(@RequestBody Map<String, Object> map) {
        Long userId = UserContext.getUserId();
        Long goodsId = Long.valueOf(map.get("goodsId").toString());
        String tradePlace = (String) map.get("tradePlace");
        String orderNote = (String) map.get("orderNote");

        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            return R.fail("商品不存在");
        }

        TradeOrder order = new TradeOrder();
        order.setBuyerId(userId);
        order.setSellerId(goods.getSellerId());
        order.setGoodsId(goodsId);
        order.setTradePlace(tradePlace);
        order.setOrderNote(orderNote);
        order.setOrderStatus("WAIT_CONFIRM");
        tradeOrderService.save(order);

        return R.ok("下单成功");
    }

    /**
     * 我的订单（我买的）
     */
    @GetMapping("/myBuy")
    public R listMyBuy() {
        Long userId = UserContext.getUserId();
        var list = tradeOrderService.lambdaQuery()
                .eq(TradeOrder::getBuyerId, userId)
                .orderByDesc(TradeOrder::getCreateTime)
                .list();
        return R.ok(list);
    }

    /**
     * 我的卖出（我卖的）
     */
    @GetMapping("/mySell")
    public R listMySell() {
        Long userId = UserContext.getUserId();
        var list = tradeOrderService.lambdaQuery()
                .eq(TradeOrder::getSellerId, userId)
                .orderByDesc(TradeOrder::getCreateTime)
                .list();
        return R.ok(list);
    }

    /**
     * 卖家确认订单
     */
    @PutMapping("/confirm/{id}")
    public R confirm(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        TradeOrder order = tradeOrderService.getById(id);
        if (order == null) {
            return R.fail("订单不存在");
        }
        if (!order.getSellerId().equals(userId)) {
            return R.fail("无权操作别人的订单");
        }
        order.setOrderStatus("WAIT_TRADE");
        order.setConfirmTime(LocalDateTime.now());
        tradeOrderService.updateById(order);
        return R.ok("已确认订单");
    }

    /**
     * 买家确认收货（完成交易）
     */
    @PutMapping("/finish/{id}")
    public R finish(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        TradeOrder order = tradeOrderService.getById(id);
        if (order == null) {
            return R.fail("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            return R.fail("无权操作别人的订单");
        }
        order.setOrderStatus("FINISH");
        order.setFinishTime(LocalDateTime.now());
        tradeOrderService.updateById(order);
        return R.ok("交易已完成");
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    public R cancel(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        TradeOrder order = tradeOrderService.getById(id);
        if (order == null) {
            return R.fail("订单不存在");
        }
        // 买家或卖家都能取消
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return R.fail("无权操作别人的订单");
        }
        order.setOrderStatus("CANCEL");
        order.setCancelTime(LocalDateTime.now());
        tradeOrderService.updateById(order);
        return R.ok("订单已取消");
    }
}
