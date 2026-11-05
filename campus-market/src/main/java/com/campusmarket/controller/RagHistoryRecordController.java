package com.campusmarket.controller;

import com.campusmarket.entity.RagHistoryRecord;
import com.campusmarket.service.RagHistoryRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagHistoryRecordController {

    @Autowired
    private RagHistoryRecordService ragHistoryRecordService;

    // 测试接口：http://localhost:8080/rag/list
    @GetMapping("/list")
    public List<RagHistoryRecord> list() {
        return ragHistoryRecordService.list();
    }
}
