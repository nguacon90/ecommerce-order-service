package com.vctek.orderservice.config;

import com.vctek.logging.LogstashConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class LogConfig extends LogstashConfiguration {
    @EventListener(ApplicationReadyEvent.class)
    public void registerLogStashAppender() {
        super.init();
    }
}
