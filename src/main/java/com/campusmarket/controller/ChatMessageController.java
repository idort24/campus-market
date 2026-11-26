package com.campusmarket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusmarket.common.R;
import com.campusmarket.entity.ChatMessage;
import com.campusmarket.entity.SysUser;
import com.campusmarket.service.ChatMessageService;
import com.campusmarket.service.SysUserService;
import com.campusmarket.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息接口
 *
 * 路径统一挂到 /api/v1/chat 下，走登录拦截器，fromUserId 从 token 取，前端无法伪造。
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 会话列表 GET /api/v1/chat/sessions
     * 按对方聚合：最后一条消息、最后时间、未读数（对方发给我且 read_flag=0）、对方昵称。
     */
    @GetMapping("/sessions")
    public R<List<Map<String, Object>>> sessions() {
        Long me = UserContext.getUserId();

        List<ChatMessage> all = chatMessageService.lambdaQuery()
                .and(w -> w.eq(ChatMessage::getSenderId, me).or().eq(ChatMessage::getReceiverId, me))
                .orderByDesc(ChatMessage::getCreateTime)
                .list();

        Map<Long, Map<String, Object>> byPeer = new LinkedHashMap<>();
        for (ChatMessage m : all) {
            Long peer = m.getSenderId().equals(me) ? m.getReceiverId() : m.getSenderId();
            if (peer == null) {
                continue;
            }
            Map<String, Object> sess = byPeer.computeIfAbsent(peer, k -> {
                Map<String, Object> s = new HashMap<>();
                s.put("userId", k);
                s.put("unreadCount", 0);
                return s;
            });
            // 列表按时间倒序，第一条即最新
            if (!sess.containsKey("lastMessage")) {
                sess.put("lastMessage", m.getMsgContent() == null ? "" : m.getMsgContent());
                sess.put("msgImage", m.getMsgImage());
                sess.put("lastTime", m.getCreateTime());
                sess.put("mineLast", m.getSenderId().equals(me));
            }
            if (m.getReceiverId().equals(me) && (m.getReadFlag() == null || m.getReadFlag() == 0)) {
                sess.put("unreadCount", ((Number) sess.get("unreadCount")).intValue() + 1);
            }
        }

        List<Long> peerIds = new ArrayList<>(byPeer.keySet());
        Map<Long, String> nickMap = new HashMap<>();
        if (!peerIds.isEmpty()) {
            List<SysUser> users = sysUserService.listByIds(peerIds);
            for (SysUser u : users) {
                nickMap.put(u.getId(), u.getNickName());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(byPeer.values());
        for (Map<String, Object> s : result) {
            Object uid = s.get("userId");
            s.put("nickName", nickMap.getOrDefault(uid, "同学"));
        }
        return R.ok(result);
    }

    /**
     * 拉取与某用户的会话历史 GET /api/v1/chat/messages?userId=对方id
     * 返回双方之间的消息，按时间正序；同时把"对方发给我"的未读消息标记为已读。
     */
    @GetMapping("/messages")
    public R<List<ChatMessage>> messages(@RequestParam("userId") Long userId) {
        Long me = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            return R.error(400, "缺少对方用户 id");
        }

        LambdaQueryWrapper<ChatMessage> q = new LambdaQueryWrapper<ChatMessage>()
                .nested(w -> w.eq(ChatMessage::getSenderId, me).eq(ChatMessage::getReceiverId, userId))
                .or()
                .nested(w -> w.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, me))
                .orderByAsc(ChatMessage::getCreateTime);
        List<ChatMessage> list = chatMessageService.list(q);

        // 进入会话即把对方发来的未读消息标已读
        ChatMessage readUpdate = new ChatMessage();
        readUpdate.setReadFlag(1);
        chatMessageService.update(readUpdate, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, userId)
                .eq(ChatMessage::getReceiverId, me)
                .eq(ChatMessage::getReadFlag, 0));

        return R.ok(list);
    }

    /**
     * 发送消息 POST /api/v1/chat/messages
     * body: { "receiverId": 对方id, "content": "文本内容", "msgImage": "可选图片地址" }
     */
    @PostMapping("/messages")
    public R<ChatMessage> send(@RequestBody Map<String, Object> body) {
        Long me = UserContext.getUserId();

        Object receiverRaw = body.get("receiverId");
        String content = body.get("content") == null ? null : String.valueOf(body.get("content")).trim();
        String msgImage = body.get("msgImage") == null ? null : String.valueOf(body.get("msgImage")).trim();

        if (receiverRaw == null) {
            return R.error(400, "缺少接收人 receiverId");
        }
        Long receiverId;
        try {
            receiverId = Long.valueOf(String.valueOf(receiverRaw));
        } catch (NumberFormatException e) {
            return R.error(400, "receiverId 格式错误");
        }
        if (receiverId.equals(me)) {
            return R.error(400, "不能给自己发消息");
        }
        if ((content == null || content.isEmpty()) && (msgImage == null || msgImage.isEmpty())) {
            return R.error(400, "消息内容不能为空");
        }

        ChatMessage m = new ChatMessage();
        m.setSenderId(me);
        m.setReceiverId(receiverId);
        m.setMsgContent(content == null ? "" : content);
        m.setMsgImage(msgImage);
        m.setIsAiMessage(0);
        m.setReadFlag(0);
        m.setRiskHint(0);
        m.setCreateTime(LocalDateTime.now());
        chatMessageService.save(m);
        return R.ok(m);
    }
}
