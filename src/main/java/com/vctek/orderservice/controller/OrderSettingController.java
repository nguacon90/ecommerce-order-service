package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.facade.OrderSettingFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order-settings")
public class OrderSettingController {

    private Validator<OrderSettingRequest> comboPriceSettingValidator;
    private OrderSettingFacade orderSettingFacade;

    @PostMapping("/combo")
    @PreAuthorize("hasAnyPermission(#orderSettingRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).SETUP_COMBO_PRICE.code())")
    public ResponseEntity<OrderSettingData> createOrUpdateComboSetting(@RequestBody OrderSettingRequest orderSettingRequest) {
        comboPriceSettingValidator.validate(orderSettingRequest);
        OrderSettingData data = orderSettingFacade.createOrUpdateComboPriceSetting(orderSettingRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/combo")
    public ResponseEntity<OrderSettingData> getOrderSettingByType(@RequestParam("companyId") Long companyId) {
        OrderSettingData data = orderSettingFacade.getComboPriceSetting(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/maximum-discount")
    public ResponseEntity<OrderSettingData> getOrderMaximumDiscount(@RequestParam("companyId") Long companyId) {
        OrderSettingData data = orderSettingFacade.getOrderMaximumDiscount(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/setup-notificaton-change-order-status")
    @PreAuthorize("hasAnyPermission(#request.companyId, T(com.vctek.util.PermissionCodes).MANAGE_COMPANY_INFORMATION.code())")
    public ResponseEntity<OrderSettingData> createOrUpdateSettingNotificationChangeStatus(@RequestBody OrderSettingRequest request) {
        OrderSettingData data = orderSettingFacade.createOrUpdateSettingNotificationChangeStatus(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/get-notificaton-change-order-status")
    @PreAuthorize("hasAnyPermission(#companyId, T(com.vctek.util.PermissionCodes).MANAGE_COMPANY_INFORMATION.code())")
    public ResponseEntity<OrderSettingData> getSettingNotificationChangeStatus(@RequestParam("companyId") Long companyId) {
        OrderSettingData data = orderSettingFacade.getSettingNotificationChangeStatus(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    @Qualifier("comboPriceSettingValidator")
    public void setComboPriceSettingValidator(Validator<OrderSettingRequest> comboPriceSettingValidator) {
        this.comboPriceSettingValidator = comboPriceSettingValidator;
    }

    @Autowired
    public void setOrderSettingFacade(OrderSettingFacade orderSettingFacade) {
        this.orderSettingFacade = orderSettingFacade;
    }
}
