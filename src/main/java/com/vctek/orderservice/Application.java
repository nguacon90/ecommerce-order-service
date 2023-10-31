package com.vctek.orderservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.Optional;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableEurekaClient
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.vctek.orderservice", "com.vctek.logging"})
@EnableCaching
@EnableFeignClients(basePackages = {"com.vctek.orderservice.feignclient"})
@EnableElasticsearchRepositories(basePackages = {"com.vctek.orderservice.elasticsearch"})
@EnableJpaRepositories(basePackages = {"com.vctek.orderservice.repository",
        "com.vctek.orderservice.**.repository"})
@EnableRedisRepositories(basePackages = {"com.vctek.orderservice.redis"})
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Optional<String> version = Optional.ofNullable(Application.class.getPackage().getImplementationVersion());
        SpringApplication.run(Application.class, args);
        String versionStr = version.orElse("");
        LOGGER.info("Order service {} started.", versionStr);
    }
}

