package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;         // 发送方userId sender_id
    private Long receiverId;       // 接收方userId receiver_id
    private String msgContent;     // 文本消息 msg_content
    private String msgImage;       // 图片OSS地址 msg_image
    private Integer isAiMessage;   // 0用户消息，1Agent智能助手自动回复 is_ai_message
    private Integer readFlag;      // 0未读，1已读 read_flag
    private Integer riskHint;      // 0无风险标记，1命中诈骗风险词 risk_hint
    private String extJson;        // 扩展，存放AI溯源日志片段 ext_json
    private LocalDateTime createTime; // 创建时间 create_time

    @TableLogic
    private Integer isDeleted;     // 逻辑删除 is_deleted
}
