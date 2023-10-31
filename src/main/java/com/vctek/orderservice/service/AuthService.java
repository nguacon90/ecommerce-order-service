package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.UserData;

import java.util.List;

public interface AuthService {
    UserData getUserById(Long userId);

    Long getCurrentUserId();

    List<Long> getAllWarehouseOfCurrentUser(Long companyId);

    List<Long> getUserWarehouses(Long userId, Long companyId);

    boolean isAdminCompanyUser();

    boolean isCheckDeliveryDate(Long companyId);

    boolean isCurrentCustomerUserOrAnonymous();
}
