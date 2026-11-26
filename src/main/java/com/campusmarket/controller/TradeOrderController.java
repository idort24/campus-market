package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.Goods;
import com.campusmarket.entity.SysUser;
import com.campusmarket.entity.TradeOrder;
import com.campusmarket.service.GoodsService;
import com.campusmarket.service.SysUserService;
import com.campusmarket.service.TradeOrderService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/order")
public class TradeOrderController {

    @Autowired
    private TradeOrderService tradeOrderService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SysUserService sysUserService;

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
        if (goods.getSellerId().equals(userId)) {
            return R.fail("不能购买自己发布的商品");
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
    public R<List<Map<String, Object>>> listMyBuy() {
        Long userId = UserContext.getUserId();
        var list = tradeOrderService.lambdaQuery()
                .eq(TradeOrder::getBuyerId, userId)
                .orderByDesc(TradeOrder::getCreateTime)
                .list();
        return R.ok(enrich(list));
    }

    /**
     * 我的卖出（我卖的）
     */
    @GetMapping("/mySell")
    public R<List<Map<String, Object>>> listMySell() {
        Long userId = UserContext.getUserId();
        var list = tradeOrderService.lambdaQuery()
                .eq(TradeOrder::getSellerId, userId)
                .orderByDesc(TradeOrder::getCreateTime)
                .list();
        return R.ok(enrich(list));
    }

    /**
     * 批量补全订单展示字段：商品标题/价格/首图、买卖双方昵称。
     * 订单表只存 id 关联，列表页需要这些冗余字段直接渲染。
     */
    private List<Map<String, Object>> enrich(List<TradeOrder> orders) {
        Set<Long> goodsIds = orders.stream().map(TradeOrder::getGoodsId).collect(Collectors.toSet());
        Set<Long> userIds = orders.stream()
                .flatMap(o -> java.util.stream.Stream.of(o.getBuyerId(), o.getSellerId()))
                .collect(Collectors.toSet());

        Map<Long, Goods> goodsMap = goodsIds.isEmpty() ? new HashMap<>()
                : goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, g -> g, (a, b) -> a));
        Map<Long, SysUser> userMap = userIds.isEmpty() ? new HashMap<>()
                : sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        List<Map<String, Object>> out = new ArrayList<>();
        for (TradeOrder o : orders) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("buyerId", o.getBuyerId());
            m.put("sellerId", o.getSellerId());
            m.put("goodsId", o.getGoodsId());
            m.put("orderStatus", o.getOrderStatus());
            m.put("tradePlace", o.getTradePlace());
            m.put("orderNote", o.getOrderNote());
            m.put("createTime", o.getCreateTime());
            m.put("confirmTime", o.getConfirmTime());
            m.put("finishTime", o.getFinishTime());
            m.put("cancelTime", o.getCancelTime());

            Goods g = goodsMap.get(o.getGoodsId());
            m.put("goodsTitle", g != null ? g.getTitle() : "商品");
            m.put("price", g != null ? g.getPrice() : null);
            m.put("cover", firstImage(g != null ? g.getImages() : null));

            SysUser buyer = userMap.get(o.getBuyerId());
            SysUser seller = userMap.get(o.getSellerId());
            m.put("buyerName", buyer != null ? buyer.getNickName() : "买家");
            m.put("sellerName", seller != null ? seller.getNickName() : "卖家");
            out.add(m);
        }
        return out;
    }

    /** 从 goods.images JSON 数组里取首图，解析失败返回 null */
    private String firstImage(String images) {
        if (images == null || images.isBlank()) return null;
        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(images);
            if (node.isArray() && node.size() > 0) {
                return node.get(0).asText();
            }
        } catch (Exception ignored) {
        }
        return null;
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
