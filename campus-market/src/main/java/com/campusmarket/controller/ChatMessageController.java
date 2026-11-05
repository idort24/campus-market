package com.campusmarket.controller;

import com.campusmarket.entity.ChatMessage;
import com.campusmarket.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    // 测试接口：http://localhost:8080/chat/list
    @GetMapping("/list")
    public List<ChatMessage> list() {
        return chatMessageService.list();
    }
}
