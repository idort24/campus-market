package com.campusmarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

/**
 * 开启 JMS 注解支持：@JmsListener 消费者才能被识别
 */
@Configuration
@EnableJms
public class MqConfig {
}
