package com.campusmarket.mq;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 生产计数持有者：记录本进程累计投递到 MQ 的事件数。
 * 仅用于积压估算（生产数 − 已处理数），不是 broker 的精确就绪深度；
 * 进程重启后计数清零，演示场景可接受。
 */
@Component
public class ConsultCounterHolder {

    private final AtomicLong produced = new AtomicLong(0);

    public void incrementProduced() {
        produced.incrementAndGet();
    }

    public long getProduced() {
        return produced.get();
    }
}
