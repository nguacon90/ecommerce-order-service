package com.vctek.orderservice.controller;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractController {
    protected AuthService authService;

    protected void validateAdminCompanyUser() {
        if(!authService.isAdminCompanyUser()) {
            ErrorCodes err = ErrorCodes.ACCESS_DENIED;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
