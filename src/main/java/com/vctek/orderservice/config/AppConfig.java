package com.vctek.orderservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.feign.config.OAuth2InterceptedFeignConfiguration;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.service.impl.KafkaProducerServiceImpl;
import com.vctek.service.DownloadExcelService;
import com.vctek.service.ObjectMapperService;
import com.vctek.service.TokenStoreService;
import com.vctek.service.impl.DefaultObjectMapperService;
import com.vctek.service.impl.DownloadExcelServiceImpl;
import com.vctek.service.impl.TokenStoreServiceImpl;
import com.vctek.sync.MutexFactory;
import com.vctek.sync.XMutexFactoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.concurrent.Executor;

@Configuration
public class AppConfig extends OAuth2InterceptedFeignConfiguration {

    @Bean
    public TokenStoreService tokenStoreService(TokenStore tokenStore) {
        return new TokenStoreServiceImpl(tokenStore);
    }

    @Bean
    public KafkaProducerService permissionFacade() {
        return new KafkaProducerServiceImpl();
    }

    @Bean
    public MutexFactory<String> mutexFactory() {
        return new XMutexFactoryImpl<>();
    }

    @Value("${vctek.executor.exportExcel.keepAliveTimeSeconds:3600}")
    private int excelExecutorKeepAliveTimeInSeconds = 3600;

    @Bean("exportExcelExecutor")
    public Executor productUpdateExecutor(@Value("${vctek.executor.exportExcel.corePoolSize:5}") int corePoolSize,
                                          @Value("${vctek.executor.exportExcel.maxPoolSize:30}") int maxPoolSize) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setThreadNamePrefix("exportExcelExecutor");
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setKeepAliveSeconds(excelExecutorKeepAliveTimeInSeconds);
        return taskExecutor;
    }

    @Bean
    public DownloadExcelService downloadExcelService() {
        return new DownloadExcelServiceImpl();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ObjectMapperService objectMapperService() {
        return new DefaultObjectMapperService();
    }
}
