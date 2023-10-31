package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.OrderSettingCustomerData;
import com.vctek.orderservice.dto.SettingCustomerData;
import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.dto.request.OrderSettingCustomerSearchRequest;
import com.vctek.orderservice.facade.OrderSettingCustomerFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("order-setting-customer")
public class OrderSettingCustomerController {

    private Validator<OrderSettingCustomerRequest> validator;
    private OrderSettingCustomerFacade facade;

    @PostMapping
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).MANAGE_INFO_CUSTOMER_IN_ORDER.code())")
    public ResponseEntity<OrderSettingCustomerData> createSetting(@RequestBody OrderSettingCustomerRequest request) {
        validator.validate(request);
        OrderSettingCustomerData data = facade.createSetting(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/default")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).MANAGE_INFO_CUSTOMER_IN_ORDER.code())")
    public ResponseEntity<OrderSettingCustomerData> createOrUpdateDefault(@RequestBody OrderSettingCustomerRequest request) {
        OrderSettingCustomerData data = facade.createOrUpdateDefault(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/default")
    public ResponseEntity<OrderSettingCustomerData> getSettingDefault(@RequestParam("companyId") Long companyId) {
        OrderSettingCustomerData data = facade.getSettingDefault(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{settingId}")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).MANAGE_INFO_CUSTOMER_IN_ORDER.code())")
    public ResponseEntity<OrderSettingCustomerData> updateSetting(@PathVariable("settingId") Long settingId,
                                                           @RequestBody OrderSettingCustomerRequest request) {
        request.setId(settingId);
        validator.validate(request);
        OrderSettingCustomerData data = facade.updateSetting(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{settingId}")
    public ResponseEntity<OrderSettingCustomerData> findOneBy(@PathVariable("settingId") Long settingId,
                                                           @RequestParam("companyId") Long companyId) {
        OrderSettingCustomerData data = facade.findOneBy(settingId, companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{settingId}/deleted")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).MANAGE_INFO_CUSTOMER_IN_ORDER.code())")
    public ResponseEntity deletedSetting(@PathVariable("settingId") Long settingId,
                                                           @RequestParam("companyId") Long companyId) {
        facade.deletedSetting(settingId, companyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{settingId}/option/{optionId}/deleted")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).MANAGE_INFO_CUSTOMER_IN_ORDER.code())")
    public ResponseEntity deletedSetting(@PathVariable("settingId") Long settingId,
                                         @PathVariable("optionId") Long optionId,
                                                           @RequestParam("companyId") Long companyId) {
        facade.deletedSettingOption(settingId, optionId, companyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<OrderSettingCustomerData>> findAllBy(OrderSettingCustomerSearchRequest request) {
        List<OrderSettingCustomerData> data = facade.findAllBy(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/order")
    public ResponseEntity<SettingCustomerData> findSettingByOrder(@RequestParam("companyId") Long companyId,
                                                                  @RequestParam("orderType") String orderType) {
        SettingCustomerData data = facade.findSettingByOrder(companyId, orderType);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    public void setValidator(Validator<OrderSettingCustomerRequest> validator) {
        this.validator = validator;
    }

    @Autowired
    public void setFacade(OrderSettingCustomerFacade facade) {
        this.facade = facade;
    }
}
