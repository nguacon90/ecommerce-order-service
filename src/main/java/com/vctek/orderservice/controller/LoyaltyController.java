package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.facade.LoyaltyFacade;
import com.vctek.validate.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("loyalty")
public class LoyaltyController {

    private LoyaltyFacade loyaltyFacade;
    private Validator<AvailablePointAmountRequest> pointAmountRequestValidator;

    public LoyaltyController(LoyaltyFacade loyaltyFacade, Validator<AvailablePointAmountRequest> pointAmountRequestValidator) {
        this.loyaltyFacade = loyaltyFacade;
        this.pointAmountRequestValidator = pointAmountRequestValidator;
    }

    @GetMapping("available-point")
    public ResponseEntity<AvailablePointAmountData> computeAvailablePointAmountOfOrder(@RequestParam Long companyId,
                       AvailablePointAmountRequest request) {
        request.setCompanyId(companyId);
        pointAmountRequestValidator.validate(request);
        AvailablePointAmountData data = loyaltyFacade.computeAvailablePointAmountOfOrder(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/check-redeem-product")
    public ResponseEntity<List<Long>> checkRedeemOfProduct(@RequestParam Long companyId, @RequestParam String products) {
        List<Long> data = loyaltyFacade.checkRedeemOfProduct(companyId, products);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
