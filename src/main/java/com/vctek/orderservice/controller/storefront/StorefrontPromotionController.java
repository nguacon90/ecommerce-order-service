package com.vctek.orderservice.controller.storefront;

import com.vctek.orderservice.dto.CommercePromotionData;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.dto.storefront.UserCouponCodeData;
import com.vctek.orderservice.facade.PromotionSourceRuleFacade;
import com.vctek.orderservice.service.CustomerCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/storefront")
public class StorefrontPromotionController {
    private PromotionSourceRuleFacade promotionSourceRuleFacade;
    private CustomerCouponService customerCouponService;

    @GetMapping("/{companyId}/promotions")
    public ResponseEntity<List<PromotionSourceRuleDTO>> findActivePromotions(@PathVariable("companyId") Long companyId) {
        List<PromotionSourceRuleDTO> data = promotionSourceRuleFacade.findAllActivePromotionsForStorefront(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{companyId}/promotions/{promotionId}")
    public ResponseEntity<CommercePromotionData> getDetail(@PathVariable("companyId") Long companyId,
                                                                      @PathVariable("promotionId") Long promotionId) {
        CommercePromotionData data = promotionSourceRuleFacade.getDetail(companyId, promotionId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{companyId}/promotions/coupon-by-user")
    public ResponseEntity<List<UserCouponCodeData>> getCouponByUser(@PathVariable("companyId") Long companyId) {
        List<UserCouponCodeData> data = customerCouponService.getCouponByUser(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    public void setPromotionSourceRuleFacade(PromotionSourceRuleFacade promotionSourceRuleFacade) {
        this.promotionSourceRuleFacade = promotionSourceRuleFacade;
    }

    @Autowired
    public void setCustomerCouponService(CustomerCouponService customerCouponService) {
        this.customerCouponService = customerCouponService;
    }
}
