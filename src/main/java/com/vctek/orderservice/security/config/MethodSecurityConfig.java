package com.vctek.orderservice.security.config;

import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.security.CustomMethodSecurityExpressionHandler;
import com.vctek.orderservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private AuthService authService;
    private PermissionFacade permissionFacade;

    @Autowired
    public MethodSecurityConfig(AuthService authService, PermissionFacade permissionFacade) {
        this.authService = authService;
        this.permissionFacade = permissionFacade;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        CustomMethodSecurityExpressionHandler expressionHandler =
                new CustomMethodSecurityExpressionHandler();
        expressionHandler.setPermissionFacade(permissionFacade);
        expressionHandler.setAuthService(authService);
        return expressionHandler;
    }
}
