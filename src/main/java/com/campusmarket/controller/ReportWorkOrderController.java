package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.entity.ReportWorkOrder;
import com.campusmarket.service.ReportWorkOrderService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
public class ReportWorkOrderController {

    @Autowired
    private ReportWorkOrderService reportWorkOrderService;

    /**
     * 提交举报
     */
    @PostMapping("/add")
    public R add(@RequestBody Map<String, Object> map) {
        Long userId = UserContext.getUserId();
        String targetType = (String) map.get("targetType"); // USER / GOODS
        Long targetId = Long.valueOf(map.get("targetId").toString());
        String reportReason = (String) map.get("reportReason");

        ReportWorkOrder report = new ReportWorkOrder();
        report.setReporterUserId(userId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReportReason(reportReason);
        report.setStatus("待处理");
        reportWorkOrderService.save(report);

        return R.ok("举报已提交，我们会尽快处理");
    }

    /**
     * 我的举报记录
     */
    @GetMapping("/my")
    public R myList() {
        Long userId = UserContext.getUserId();
        var list = reportWorkOrderService.lambdaQuery()
                .eq(ReportWorkOrder::getReporterUserId, userId)
                .orderByDesc(ReportWorkOrder::getCreateTime)
                .list();
        return R.ok(list);
    }
}
