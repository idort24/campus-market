package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.Evaluation;
import com.campusmarket.service.EvaluationService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 发表评价
     */
    @PostMapping("/add")
    public R add(@RequestBody Map<String, Object> map) {
        Long userId = UserContext.getUserId();
        Long orderId = Long.valueOf(map.get("orderId").toString());
        Long toUserId = Long.valueOf(map.get("toUserId").toString());
        Integer score = Integer.valueOf(map.get("score").toString());
        String evalContent = (String) map.get("evalContent");

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
}
