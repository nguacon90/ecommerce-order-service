package com.vctek.orderservice.controller;

import com.vctek.dto.redis.OrderStorefrontSetupData;
import com.vctek.orderservice.facade.OrderStorefrontSetupFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-storefront-setup")
public class OrderStorefrontSetupController {
    private OrderStorefrontSetupFacade facade;
    private Validator<OrderStorefrontSetupData> validator;

    public OrderStorefrontSetupController(OrderStorefrontSetupFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @PreAuthorize("hasAnyPermission(#request.companyId, T(com.vctek.util.PermissionCodes).ECOMMERCE_WEBSITE_SETTING_MANAGEMENT.code())")
    public ResponseEntity<OrderStorefrontSetupData> createOrUpdate(@RequestBody OrderStorefrontSetupData request) {
        validator.validate(request);
        OrderStorefrontSetupData data = facade.createOrUpdate(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<OrderStorefrontSetupData> findByCompanyId(@RequestParam("companyId") Long companyId) {
        OrderStorefrontSetupData data = facade.findByCompanyId(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    public void setValidator(Validator<OrderStorefrontSetupData> validator) {
        this.validator = validator;
    }
}
