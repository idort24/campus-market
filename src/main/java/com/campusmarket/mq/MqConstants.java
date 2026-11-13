package com.campusmarket.mq;

/**
 * MQ 队列 / 地址常量
 */
public final class MqConstants {

    private MqConstants() {
    }

    /** 咨询事件主队列（anycast，一条消息只被一个消费者抢到） */
    public static final String CONSULT_QUEUE = "consult.queue";

    /** 死信队列地址：达到最大投递次数或解析失败的异常消息转入这里等待排查 */
    public static final String DLQ_ADDRESS = "DLQ";
}
