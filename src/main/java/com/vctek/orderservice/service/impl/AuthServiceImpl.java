package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.UserClient;
import com.vctek.orderservice.service.AuthService;
import com.vctek.service.TokenStoreService;
import com.vctek.service.UserService;
import com.vctek.util.AccountRoles;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private UserClient userClient;
    private TokenStoreService tokenStoreService;
    private CompanyClient companyClient;
    private UserService userService;

    @Autowired
    public AuthServiceImpl(UserClient userClient, TokenStoreService tokenStoreService) {
        this.userClient = userClient;
        this.tokenStoreService = tokenStoreService;
    }

    @Override
    @Cacheable(unless="#result == null", value = "user", key = "#userId", cacheManager = "microServiceCacheManager")
    public UserData getUserById(Long userId) {
        return userClient.getUserById(userId);
    }

    @Override
    public Long getCurrentUserId() {
        return tokenStoreService.getCurrentUserId();
    }

    @Override
    public List<Long> getAllWarehouseOfCurrentUser(Long companyId) {
        return userClient.getAllWarehouses(getCurrentUserId(), companyId);
    }

    @Override
    public List<Long> getUserWarehouses(Long userId, Long companyId) {
        return userClient.getAllWarehouseByUser(userId, companyId);
    }

    @Override
    public boolean isAdminCompanyUser() {
        Boolean adminCompanyUser = userClient.isAdminCompanyUser();
        return Boolean.TRUE.equals(adminCompanyUser);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "compulsory_delivery_date", key = "#companyId", cacheManager = "microServiceCacheManager")
    public boolean isCheckDeliveryDate(Long companyId) {
        return companyClient.getCompulsoryDeliveryDate(companyId);
    }

    @Override
    public boolean isCurrentCustomerUserOrAnonymous() {
        Long currentUserId = userService.getCurrentUserId();
        if(currentUserId == null) {
            return true;
        }
        List<String> authorities = userService.getAuthorities();
        if(CollectionUtils.isEmpty(authorities)) {
            return true;
        }

        if(authorities.contains(AccountRoles.ROLE_MERCHANT.role()) ||
            authorities.contains(AccountRoles.ROLE_EMPLOYEE.role())) {
            return false;
        }

        return true;
    }

    @Autowired
    public void setCompanyClient(CompanyClient companyClient) {
        this.companyClient = companyClient;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
