package com.campusmarket.config;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 内嵌 Artemis broker 可靠性配置：
 * - 最大投递次数 3 次，超过后转入死信队列 DLQ
 * - 重投间隔 1 秒
 * - 自动创建死信资源
 * 消息持久化仍由 spring.artemis.embedded.persistent=false 关闭（演示场景）。
 */
@Configuration
public class ArtemisBrokerConfig {

    @Bean
    public ArtemisConfigurationCustomizer artemisConfigurationCustomizer() {
        return config -> {
            AddressSettings settings = new AddressSettings()
                    .setMaxDeliveryAttempts(3)
                    .setRedeliveryDelay(1000L)
                    .setDeadLetterAddress(new SimpleString("DLQ"))
                    .setAutoCreateDeadLetterResources(true);
            config.addAddressSetting("#", settings);
        };
    }
}
