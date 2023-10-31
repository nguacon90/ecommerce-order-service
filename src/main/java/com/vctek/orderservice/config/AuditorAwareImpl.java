package com.vctek.orderservice.config;

import com.vctek.service.TokenStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Autowired
    private TokenStoreService tokenStoreService;

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(tokenStoreService.getCurrentUserId());
    }
}
