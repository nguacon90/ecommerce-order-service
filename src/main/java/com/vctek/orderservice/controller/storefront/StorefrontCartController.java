package com.vctek.orderservice.controller.storefront;

import com.vctek.orderservice.dto.CommerceCartData;
import com.vctek.orderservice.dto.CreateCartParam;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.dto.StorefrontOrderEntryDTO;
import com.vctek.orderservice.dto.request.AppliedCouponRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontSubOrderEntryRequest;
import com.vctek.orderservice.facade.CommerceCartFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storefront")
public class StorefrontCartController {

    private CommerceCartFacade commerceCartFacade;
    private Validator<StorefrontOrderEntryDTO> storefrontCartEntryValidator;
    private Validator<AppliedCouponRequest> appliedCouponRequestValidator;

    public StorefrontCartController(CommerceCartFacade commerceCartFacade) {
        this.commerceCartFacade = commerceCartFacade;
    }

    @GetMapping("/{companyId}/carts/{cartCode}/mini-cart")
    public ResponseEntity<MiniCartData> getMiniCart(@PathVariable("companyId") Long companyId,
                                                         @PathVariable("cartCode") String cartCode) {
        MiniCartData data = commerceCartFacade.getMiniCart(companyId, cartCode);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{companyId}/carts/{cartCode}")
    public ResponseEntity<CommerceCartData> getCartDetail(@PathVariable("companyId") Long companyId,
                                                          @PathVariable("cartCode") String cartCode) {
        CommerceCartData data = commerceCartFacade.getCartDetail(companyId, cartCode);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{companyId}/carts")
    public ResponseEntity<CommerceCartData> getOrCreateNewCart(@PathVariable("companyId") Long companyId,
                                                  @RequestParam(value = "oldCartGuid", required = false) String oldCartGuid,
                                               @RequestParam(value = "sellSignal", defaultValue = "ECOMMERCE_WEB") String sellSignal) {
        CreateCartParam param = new CreateCartParam();
        param.setCompanyId(companyId);
        param.setOldCartGuid(oldCartGuid);
        param.setSellSignal(sellSignal);
        CommerceCartData data = commerceCartFacade.getOrCreateNewCart(param);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{companyId}/carts/{cartCode}/entries")
    public ResponseEntity<CommerceCartData> addEntry(@PathVariable("companyId") Long companyId,
                                             @PathVariable("cartCode") String cartCode,
                                             @RequestBody StorefrontOrderEntryDTO storefrontOrderEntryDTO) {
        storefrontOrderEntryDTO.setCompanyId(companyId);
        storefrontOrderEntryDTO.setOrderCode(cartCode);
        storefrontCartEntryValidator.validate(storefrontOrderEntryDTO);
        CommerceCartData cartData = commerceCartFacade.addToCart(storefrontOrderEntryDTO);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PutMapping("/{companyId}/carts/{cartCode}/entries/{entryId}")
    public ResponseEntity<CommerceCartData> updateQuantityForCartEntry(@PathVariable("companyId") Long companyId,
                                                                       @PathVariable("cartCode") String cartCode,
                                                               @PathVariable("entryId") Long entryId,
                                                               @RequestBody StorefrontOrderEntryDTO orderEntryDTO,
                                                               @RequestParam(value = "time", required = false) Long timeRequest) {
        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        orderEntryDTO.setCompanyId(companyId);
        CommerceCartData data = commerceCartFacade.updateCartEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{companyId}/carts/{cartCode}/entries/{entryId}/sub-entries/{subEntryId}/products")
    public ResponseEntity<CommerceCartData> changeProductInCombo(@PathVariable("companyId") Long companyId,
                                                                 @PathVariable("cartCode") String cartCode,
                                                                 @PathVariable("entryId") Long entryId,
                                                                 @PathVariable("subEntryId") Long subEntryId,
                                                                 @RequestBody StoreFrontSubOrderEntryRequest subOrderEntryRequest) {
        subOrderEntryRequest.setEntryId(entryId);
        subOrderEntryRequest.setOrderCode(cartCode);
        subOrderEntryRequest.setCompanyId(companyId);
        subOrderEntryRequest.setSubEntryId(subEntryId);
        CommerceCartData data = commerceCartFacade.changeProductInCombo(subOrderEntryRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{companyId}/carts/{cartCode}/apply-coupon")
    public ResponseEntity<CommerceCartData> applyCoupon(@PathVariable("companyId") Long companyId,
                                                @PathVariable("cartCode") String cartCode,
                                                @RequestBody AppliedCouponRequest appliedCouponRequest) {
        appliedCouponRequest.setOrderCode(cartCode);
        appliedCouponRequest.setCompanyId(companyId);
        appliedCouponRequestValidator.validate(appliedCouponRequest);
        CommerceCartData data = commerceCartFacade.applyCoupon(appliedCouponRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{companyId}/carts/{cartCode}/remove-coupon")
    public ResponseEntity<CommerceCartData> removeCoupon(@PathVariable("companyId") Long companyId,
                                                 @PathVariable("cartCode") String cartCode,
                                                 @RequestBody AppliedCouponRequest appliedCouponRequest) {
        appliedCouponRequest.setOrderCode(cartCode);
        appliedCouponRequest.setCompanyId(companyId);
        appliedCouponRequestValidator.validate(appliedCouponRequest);
        CommerceCartData data = commerceCartFacade.removeCoupon(appliedCouponRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{companyId}/carts/{cartCode}/applied-promotion/{promotionSourceRuleId}")
    public ResponseEntity<CommerceCartData> appliedPromotion(@PathVariable("companyId") Long companyId,
                                                     @PathVariable("cartCode") String cartCode,
                                                     @PathVariable("promotionSourceRuleId") Long promotionSourceRuleId) {
        CommerceCartData data = commerceCartFacade.appliedPromotion(cartCode, companyId, promotionSourceRuleId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{companyId}/carts/{cartCode}/change-gift/off")
    public ResponseEntity resetChangeGift(@PathVariable("companyId") Long companyId,
                                         @PathVariable("cartCode") String cartCode) {
        commerceCartFacade.resetChangeGift(cartCode, companyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Autowired
    @Qualifier("storefrontCartEntryValidator")
    public void setStorefrontCartEntryValidator(Validator<StorefrontOrderEntryDTO> storefrontCartEntryValidator) {
        this.storefrontCartEntryValidator = storefrontCartEntryValidator;
    }

    @Autowired
    public void setAppliedCouponRequestValidator(Validator<AppliedCouponRequest> appliedCouponRequestValidator) {
        this.appliedCouponRequestValidator = appliedCouponRequestValidator;
    }
}
