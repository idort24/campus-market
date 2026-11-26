package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.Evaluation;
import com.campusmarket.entity.TradeOrder;
import com.campusmarket.service.EvaluationService;
import com.campusmarket.service.TradeOrderService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    private TradeOrderService tradeOrderService;

    /**
     * 发表评价（仅买家对卖家、且订单已完成；同一订单只能评价一次）
     */
    @PostMapping("/add")
    public R add(@RequestBody Map<String, Object> map) {
        Long userId = UserContext.getUserId();

        Long orderId = parseLong(map.get("orderId"));
        Long toUserId = parseLong(map.get("toUserId"));
        Integer score = parseInt(map.get("score"));
        String evalContent = map.get("evalContent") == null ? "" : String.valueOf(map.get("evalContent")).trim();

        if (orderId == null || toUserId == null) {
            return R.error(400, "缺少订单或被评价人");
        }
        if (score == null || score < 1 || score > 5) {
            return R.error(400, "评分需为 1~5 星");
        }
        if (evalContent.isEmpty()) {
            return R.error(400, "评价内容不能为空");
        }
        if (evalContent.length() > 500) {
            return R.error(400, "评价内容不能超过 500 字");
        }

        TradeOrder order = tradeOrderService.getById(orderId);
        if (order == null) {
            return R.fail("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            return R.fail("只能买家评价卖家");
        }
        if (!"FINISH".equals(order.getOrderStatus())) {
            return R.fail("交易完成后才能评价");
        }
        if (!order.getSellerId().equals(toUserId)) {
            return R.fail("评价对象与订单卖家不一致");
        }

        Long dup = evaluationService.lambdaQuery()
                .eq(Evaluation::getFromUserId, userId)
                .eq(Evaluation::getOrderId, orderId)
                .count();
        if (dup > 0) {
            return R.fail("该订单已评价过");
        }

        Evaluation eval = new Evaluation();
        eval.setOrderId(orderId);
        eval.setFromUserId(userId);
        eval.setToUserId(toUserId);
        eval.setScore(score);
        eval.setEvalContent(evalContent);
        eval.setIsValid(1);
        evaluationService.save(eval);

        return R.ok("评价成功");
    }

    /**
     * 我的评价（我给别人的）
     */
    @GetMapping("/my")
    public R myList() {
        Long userId = UserContext.getUserId();
        var list = evaluationService.lambdaQuery()
                .eq(Evaluation::getFromUserId, userId)
                .orderByDesc(Evaluation::getCreateTime)
                .list();
        return R.ok(list);
    }

    /**
     * 某个人收到的评价
     */
    @GetMapping("/toUser/{userId}")
    public R listByToUser(@PathVariable Long userId) {
        var list = evaluationService.lambdaQuery()
                .eq(Evaluation::getToUserId, userId)
                .eq(Evaluation::getIsValid, 1)
                .orderByDesc(Evaluation::getCreateTime)
                .list();
        return R.ok(list);
    }

    private Long parseLong(Object raw) {
        if (raw == null) return null;
        try {
            return Long.valueOf(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(Object raw) {
        if (raw == null) return null;
        try {
            return Integer.valueOf(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
