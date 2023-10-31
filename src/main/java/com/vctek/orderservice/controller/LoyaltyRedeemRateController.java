package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.LoyaltyRedeemRateData;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.facade.RedeemRateFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loyalty-redeem-rate")
public class LoyaltyRedeemRateController {

    private RedeemRateFacade facade;
    private Validator<RedeemRateRequest> validator;

    @PostMapping("/product")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<List> createOrUpdateUseProduct(@RequestBody RedeemRateRequest request) {
        validator.validate(request);
        List<Long> data = facade.createOrUpdateProduct(request);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @PostMapping("/category")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<List> createOrUpdateUseCategory(@RequestBody RedeemRateRequest request) {
        validator.validate(request);
        List<Long> data = facade.createOrUpdateCategory(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/delete-category")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<Void> deleteCategory(@RequestParam Long companyId,
                                               @RequestBody RedeemRateRequest request) {
        request.setCompanyId(companyId);
        facade.deleteCategory(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/delete-product")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<Void> deleteProduct(@RequestParam Long companyId,
                                               @RequestBody LoyaltyRewardRateDetailRequest request) {
        request.setCompanyId(companyId);
        facade.deleteProduct(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<LoyaltyRedeemRateData> findAllRewardRateUse(@RequestParam("companyId") Long companyId) {
        LoyaltyRedeemRateData data = facade.findBy(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    public void setFacade(RedeemRateFacade facade) {
        this.facade = facade;
    }

    @Autowired
    public void setLoyaltyRewardRateValidator(Validator<RedeemRateRequest> validator) {
        this.validator = validator;
    }
}
