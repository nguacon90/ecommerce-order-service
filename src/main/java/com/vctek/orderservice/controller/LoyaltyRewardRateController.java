package com.vctek.orderservice.controller;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.AllLoyaltyRewardRateData;
import com.vctek.orderservice.dto.CategoryLoyaltyRewardRateData;
import com.vctek.orderservice.dto.DefaultLoyaltyRewardRateData;
import com.vctek.orderservice.dto.ProductLoyaltyRewardRateData;
import com.vctek.orderservice.dto.excel.ProductLoyaltyRewardRateDTO;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.LoyaltyRewardRateFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/loyalty-reward-rate")
public class LoyaltyRewardRateController {

    private LoyaltyRewardRateFacade loyaltyRewardRateFacade;
    private Validator<LoyaltyRewardRateRequest> loyaltyRewardRateValidator;

    @GetMapping
    public ResponseEntity<AllLoyaltyRewardRateData> findDetail(@RequestParam("companyId") Long companyId) {
        AllLoyaltyRewardRateData data = loyaltyRewardRateFacade.findBy(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("default")
    @PreAuthorize("hasAnyPermission(#loyaltyRewardRateDetailRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<DefaultLoyaltyRewardRateData> createOrUpdateDefault(@RequestBody LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest) {
        validateCompanyId(loyaltyRewardRateDetailRequest.getCompanyId());
        validateRewardRate(loyaltyRewardRateDetailRequest.getRewardRate());
        DefaultLoyaltyRewardRateData data = loyaltyRewardRateFacade.createOrUpdateDefault(loyaltyRewardRateDetailRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("product")
    @PreAuthorize("hasAnyPermission(#loyaltyRewardRateRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<List<ProductLoyaltyRewardRateData>> createOrUpdateProduct(@RequestBody LoyaltyRewardRateRequest loyaltyRewardRateRequest) {
        loyaltyRewardRateValidator.validate(loyaltyRewardRateRequest);
        List<ProductLoyaltyRewardRateData> data = loyaltyRewardRateFacade.createOrUpdateProduct(loyaltyRewardRateRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("product-delete")
    @PreAuthorize("hasAnyPermission(#loyaltyRewardRateDetailRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity deleteProduct(@RequestBody LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest) {
        validateCompanyId(loyaltyRewardRateDetailRequest.getCompanyId());
        loyaltyRewardRateFacade.deleteProduct(loyaltyRewardRateDetailRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("category-delete")
    @PreAuthorize("hasAnyPermission(#loyaltyRewardRateDetailRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity deleteCategory(@RequestBody LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest) {
        validateCompanyId(loyaltyRewardRateDetailRequest.getCompanyId());
        loyaltyRewardRateFacade.deleteCategory(loyaltyRewardRateDetailRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("category")
    @PreAuthorize("hasAnyPermission(#loyaltyRewardRateRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<List<CategoryLoyaltyRewardRateData>> createOrUpdateCategory(@RequestBody LoyaltyRewardRateRequest loyaltyRewardRateRequest) {
        loyaltyRewardRateValidator.validate(loyaltyRewardRateRequest);
        List<CategoryLoyaltyRewardRateData> data = loyaltyRewardRateFacade.createOrUpdateCategory(loyaltyRewardRateRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("import-excel-product")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).LOYALTY_REWARD_SETTING.code())")
    public ResponseEntity<ProductLoyaltyRewardRateDTO> importExcelProduct(
            @RequestParam("companyId") Long companyId,
            @RequestParam("file") MultipartFile multipartFile) {
        ProductLoyaltyRewardRateDTO data = loyaltyRewardRateFacade.importExcelProduct(companyId, multipartFile);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    private void validateCompanyId(Long companyId) {
        if (companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validateRewardRate(Double rewardRate) {
        if (rewardRate < 0) {
            ErrorCodes err = ErrorCodes.INVALID_REWARD_RATE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setLoyaltyRewardRateFacade(LoyaltyRewardRateFacade loyaltyRewardRateFacade) {
        this.loyaltyRewardRateFacade = loyaltyRewardRateFacade;
    }

    @Autowired
    @Qualifier("loyaltyRewardRateValidator")
    public void setLoyaltyRewardRateValidator(Validator<LoyaltyRewardRateRequest> loyaltyRewardRateValidator) {
        this.loyaltyRewardRateValidator = loyaltyRewardRateValidator;
    }
}
