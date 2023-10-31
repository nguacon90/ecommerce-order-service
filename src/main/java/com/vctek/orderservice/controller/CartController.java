package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.facade.CartFacade;
import com.vctek.orderservice.facade.PermissionFacade;
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
@RequestMapping("/qtn/carts")
public class CartController {
    private CartFacade cartFacade;
    private Validator<CartInfoParameter> createCartValidator;
    private Validator<OrderEntryDTO> cartEntryValidator;
    private Validator<RefreshCartRequest> refreshCartRequestValidator;
    private Validator<AppliedCouponRequest> appliedCouponRequestValidator;
    private PermissionFacade permissionFacade;
    private Validator<AddSubOrderEntryRequest> addSubOrderEntryRequestValidator;
    private Validator<RemoveSubOrderEntryRequest> removeSubOrderEntryRequestValidator;
    private Validator<ToppingOptionRequest> toppingOptionRequestValidator;
    private Validator<ToppingItemRequest> toppingItemRequestValidator;

    public CartController(CartFacade cartFacade,
                          Validator<CartInfoParameter> createCartValidator,
                          @Qualifier("cartEntryValidator") Validator<OrderEntryDTO> cartEntryValidator) {
        this.cartFacade = cartFacade;
        this.createCartValidator = createCartValidator;
        this.cartEntryValidator = cartEntryValidator;
    }

    @GetMapping("/{cartCode}")
    public ResponseEntity<CartData> getDetail(@PathVariable("cartCode") String cartCode,
                                              CartInfoParameter cartInfoParameter) {
        cartInfoParameter.setCode(cartCode);
        CartData cartData = cartFacade.getDetail(cartInfoParameter);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/refresh")
    public ResponseEntity<CartData> refreshCart(@PathVariable("cartCode") String cartCode,
                                                @RequestBody RefreshCartRequest refreshCartRequest) {
        refreshCartRequest.setCode(cartCode);
        refreshCartRequestValidator.validate(refreshCartRequest);
        CartData cartData = cartFacade.refresh(refreshCartRequest);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CartData> createCart(@RequestBody CartInfoParameter cartInfoParameter) {
        createCartValidator.validate(cartInfoParameter);
        CartData cartData = cartFacade.createNewCart(cartInfoParameter);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }
    @PostMapping("/{cartCode}/upload-images")
    public ResponseEntity createImage(@PathVariable("cartCode") String cartCode,
                                                @RequestParam("companyId") Long companyId,
                                                @RequestBody OrderImagesRequest request){

        request.setCompanyId(companyId);
        cartFacade.createNewImageInCart(request,cartCode);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{cartCode}/delete")
    public ResponseEntity deleteCart(@PathVariable("cartCode") String cartCode,
                                     @RequestBody CartInfoParameter cartInfoParameter) {
        cartInfoParameter.setCode(cartCode);
        cartFacade.remove(cartInfoParameter);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{cartCode}/order-source")
    public ResponseEntity<CartData> changeOrderSource(@PathVariable("cartCode") String cartCode,
                                     @RequestBody CartInfoParameter cartInfoParameter) {
        cartInfoParameter.setCode(cartCode);
        CartData cartData = cartFacade.changeOrderSource(cartInfoParameter);
        return new ResponseEntity(cartData, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries")
    public ResponseEntity<CartData> addCartEntry(@PathVariable("cartCode") String cartCode,
                                                 @RequestBody OrderEntryDTO orderEntryDTO) {
        orderEntryDTO.setOrderCode(cartCode);
        cartEntryValidator.validate(orderEntryDTO);
        CartData cartData = cartFacade.addToCart(orderEntryDTO);
        return new ResponseEntity(cartData, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/delete")
    public ResponseEntity<CartData> removeCartEntry(@PathVariable("cartCode") String cartCode,
                                                    @PathVariable("entryId") Long entryId,
                                                    @RequestParam("companyId") Long companyId,
                                                    @RequestParam(value = "time", required = false) Long timeRequest) {
        OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setQuantity(0l);
        orderEntryDTO.setCompanyId(companyId);
        orderEntryDTO.setTimeRequest(timeRequest);
        CartData data = cartFacade.updateCartEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}")
    public ResponseEntity<CartData> updateQuantityForCartEntry(@PathVariable("cartCode") String cartCode,
                                                               @PathVariable("entryId") Long entryId,
                                                               @RequestBody OrderEntryDTO orderEntryDTO,
                                                               @RequestParam(value = "time", required = false) Long timeRequest) {
        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        CartData data = cartFacade.updateCartEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/price")
    public ResponseEntity<CartData> updatePriceForCartEntry(@PathVariable("cartCode") String cartCode,
                                                            @PathVariable("entryId") Long entryId,
                                                            @RequestBody OrderEntryDTO orderEntryDTO,
                                                            @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateCartPrice(orderEntryDTO, cartCode);

        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        CartData data = cartFacade.updatePriceCartEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/recommended-retail-price")
    public ResponseEntity<CartData> updateRecommendedRetailPriceForCartEntry(@PathVariable("cartCode") String cartCode,
                                                            @PathVariable("entryId") Long entryId,
                                                            @RequestBody OrderEntryDTO orderEntryDTO,
                                                            @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateRecommendedRetailPriceCart(orderEntryDTO, cartCode);

        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        CartData data = cartFacade.updateRecommendedRetailPriceForCartEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/discount")
    public ResponseEntity<CartData> updateDiscountForCartEntry(@PathVariable("cartCode") String cartCode,
                                                               @PathVariable("entryId") Long entryId,
                                                               @RequestBody OrderEntryDTO orderEntryDTO,
                                                               @RequestParam(value = "time", required = false) Long timeRequest) {
        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        if(!cartFacade.isSaleOffEntry(orderEntryDTO)) {
            permissionFacade.checkUpdateCartDiscount(orderEntryDTO.getCompanyId(), cartCode);
        }

        CartData data = cartFacade.updateDiscountOfCartEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/discount")
    public ResponseEntity<CartData> updateDiscountForCart(@PathVariable("cartCode") String cartCode,
                                                          @RequestBody CartDiscountRequest cartDiscountRequest,
                                                          @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateCartDiscount(cartDiscountRequest.getCompanyId(), cartCode);

        cartDiscountRequest.setCode(cartCode);
        cartDiscountRequest.setTimeRequest(timeRequest);
        CartData data = cartFacade.updateDiscountOfCart(cartDiscountRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/vat")
    public ResponseEntity<CartData> updateVatForCart(@PathVariable("cartCode") String cartCode,
                                                     @RequestBody VatRequest vatRequest,
                                                     @RequestParam(value = "time", required = false) Long timeRequest) {
        vatRequest.setCode(cartCode);
        vatRequest.setTimeRequest(timeRequest);
        CartData data = cartFacade.updateVatOfCart(vatRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/weight")
    public ResponseEntity<CartData> updateWeightForCartEntry(@PathVariable("cartCode") String cartCode,
                                                             @PathVariable("entryId") Long entryId,
                                                             @RequestBody OrderEntryDTO orderEntryDTO) {
        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setEntryId(entryId);
        CartData data = cartFacade.updateWeightForCartEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/apply-coupon")
    public ResponseEntity<CartData> applyCoupon(@PathVariable("cartCode") String cartCode,
                                                @RequestBody AppliedCouponRequest appliedCouponRequest) {
        appliedCouponRequest.setOrderCode(cartCode);
        appliedCouponRequestValidator.validate(appliedCouponRequest);
        CartData data = cartFacade.applyCoupon(appliedCouponRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/remove-coupon")
    public ResponseEntity<CartData> removeCoupon(@PathVariable("cartCode") String cartCode,
                                                 @RequestBody AppliedCouponRequest appliedCouponRequest) {
        appliedCouponRequest.setOrderCode(cartCode);
        appliedCouponRequestValidator.validate(appliedCouponRequest);
        CartData data = cartFacade.removeCoupon(appliedCouponRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/sub-entries")
    public ResponseEntity<CartData> addProductToCombo(@PathVariable("cartCode") String cartCode,
                                                            @PathVariable("entryId") Long entryId,
                                                            @RequestBody AddSubOrderEntryRequest request) {
        request.setOrderCode(cartCode);
        request.setEntryId(entryId);
        addSubOrderEntryRequestValidator.validate(request);
        CartData cartData = cartFacade.addProductToCombo(request);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/change-to-combo")
    public ResponseEntity<CartData> addComboToOrderIndirectly(@PathVariable("cartCode") String cartCode,
                                                              @PathVariable("entryId") Long entryId,
                                                              @RequestBody AddSubOrderEntryRequest request) {
        request.setOrderCode(cartCode);
        request.setEntryId(entryId);
        addSubOrderEntryRequestValidator.validate(request);
        CartData data = cartFacade.addComboToOrderIndirectly(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/remove-sub-entries")
    public ResponseEntity<Void> removeSubEntry(@PathVariable("cartCode") String cartCode,
                                               @PathVariable("entryId") Long entryId,
                                               @RequestBody RemoveSubOrderEntryRequest request) {
        request.setOrderCode(cartCode);
        request.setEntryId(entryId);
        removeSubOrderEntryRequestValidator.validate(request);

        cartFacade.removeSubEntry(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/import")
    public <T extends AbstractOrderData> ResponseEntity<T> importOnlineOrder(@PathVariable("cartCode") String cartCode,
                                                                                 @RequestParam("companyId") Long companyId,
                                                                                 @RequestParam("file") MultipartFile multipartFile) {
        T data = cartFacade.importOrderItem(cartCode, companyId, multipartFile);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/applied-promotion/{promotionSourceRuleId}")
    public ResponseEntity<CartData> appliedPromotion(@PathVariable("cartCode") String cartCode,
                                                              @RequestParam("companyId") Long companyId,
                                                              @PathVariable("promotionSourceRuleId") Long promotionSourceRuleId) {
        CartData data = cartFacade.appliedPromotion(cartCode, companyId, promotionSourceRuleId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/topping-options")
    public ResponseEntity<CartData> addToppingOptionToCart(@PathVariable("cartCode") String cartCode,
                                                                @PathVariable("entryId") Long entryId,
                                                                @RequestBody ToppingOptionRequest request) {
        request.setEntryId(entryId);
        toppingOptionRequestValidator.validate(request);
        CartData cartData = cartFacade.addToppingOption(request, cartCode);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/topping-options/{optionId}")
    public ResponseEntity<CartData> updateToppingOption(@PathVariable("cartCode") String cartCode,
                                                        @PathVariable("entryId") Long entryId,
                                                        @PathVariable("optionId") Long optionId,
                                                        @RequestBody ToppingOptionRequest request,
                                                        @RequestParam(value = "time", required = false) Long timeRequest) {
        request.setEntryId(entryId);
        request.setId(optionId);
        request.setTimeRequest(timeRequest);
        toppingOptionRequestValidator.validate(request);
        CartData cartData = cartFacade.updateToppingOption(request, cartCode);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/topping-options/{optionId}/delete")
    public ResponseEntity<CartData> removeToppingOption(@PathVariable("cartCode") String cartCode,
                                                              @PathVariable("entryId") Long entryId,
                                                              @PathVariable("optionId") Long optionId,
                                                              @RequestParam("companyId") Long companyId) {
        ToppingOptionRequest request = new ToppingOptionRequest();
        request.setEntryId(entryId);
        request.setId(optionId);
        request.setCompanyId(companyId);
        request.setQuantity(0);
        toppingOptionRequestValidator.validate(request);
        CartData cartData = cartFacade.updateToppingOption(request, cartCode);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/topping-options/{optionId}/items")
    public ResponseEntity<CartData> addToppingItem(@PathVariable("cartCode") String cartCode,
                                                             @PathVariable("optionId") Long optionId,
                                                             @RequestBody ToppingItemRequest request,
                                                             @PathVariable("entryId") Long entryId) {
        request.setEntryId(entryId);
        request.setToppingOptionId(optionId);
        request.setOrderCode(cartCode);
        toppingItemRequestValidator.validate(request);
        CartData cartData = cartFacade.addToppingItem(request);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/topping-options/{optionId}/items/{itemId}")
    public ResponseEntity<CartData> updateToppingItems(@PathVariable("cartCode") String cartCode,
                                                       @PathVariable("optionId") Long optionId,
                                                       @PathVariable("entryId") Long entryId,
                                                       @PathVariable("itemId") Long itemId,
                                                       @RequestBody ToppingItemRequest request,
                                                       @RequestParam(value = "time", required = false) Long timeRequest) {
        request.setId(itemId);
        request.setToppingOptionId(optionId);
        request.setOrderCode(cartCode);
        request.setEntryId(entryId);
        request.setTimeRequest(timeRequest);
        toppingItemRequestValidator.validate(request);
        CartData cartData = cartFacade.updateToppingItem(request);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/{entryId}/topping-options/{optionId}/items/{itemId}/discount")
    public ResponseEntity<CartData> updateDiscountForToppingItem(@PathVariable("cartCode") String cartCode,
                                                                 @PathVariable("optionId") Long optionId,
                                                                 @PathVariable("entryId") Long entryId,
                                                                 @PathVariable("itemId") Long itemId,
                                                                 @RequestBody ToppingItemRequest request,
                                                                 @RequestParam(value = "time", required = false) Long timeRequest) {

        request.setId(itemId);
        request.setToppingOptionId(optionId);
        request.setOrderCode(cartCode);
        request.setEntryId(entryId);
        request.setTimeRequest(timeRequest);
        toppingItemRequestValidator.validate(request);
        CartData data = cartFacade.updateDiscountForToppingItem(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/delete-list")
    public ResponseEntity<CartData> removeListCartEntry(@PathVariable("cartCode") String cartCode,
                                                        @RequestBody EntryRequest request) {
        request.setOrderCode(cartCode);
        CartData data = cartFacade.removeListCartEntry(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{cartCode}/loyalty-points")
    public ResponseEntity<AwardLoyaltyData> getLoyaltyPoints(@PathVariable("cartCode") String cartCode,
                                                              @RequestParam("companyId") Long companyId) {
        AwardLoyaltyData loyaltyPoints = cartFacade.getLoyaltyPointsFor(cartCode, companyId);
        return new ResponseEntity<>(loyaltyPoints, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/entries/price")
    @PreAuthorize("hasAnyPermission(#cartInfoParameter.companyId, T(com.vctek.util.PermissionCodes).CAN_EDIT_TYPE_PRICE_ONLINE.code())")
    public ResponseEntity<CartData> updatePriceForCartEntries(@PathVariable("cartCode") String cartCode,
                                                              @RequestBody CartInfoParameter cartInfoParameter) {
        cartInfoParameter.setCode(cartCode);
        CartData data = cartFacade.updatePriceForCartEntries(cartInfoParameter);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/check-discount-maximum")
    public ResponseEntity<List<OrderSettingDiscountData>> checkDiscountMaximum(@PathVariable("cartCode") String cartCode,
                                                                           @RequestParam("companyId") Long companyId) {
        List<OrderSettingDiscountData> data = cartFacade.checkDiscountMaximum(companyId, cartCode);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/update-all-discount")
    public ResponseEntity updateAllDiscount(@PathVariable("cartCode") String cartCode,
                                     @RequestBody UpdateAllDiscountRequest updateAllDiscountRequest) {
        CartData data = cartFacade.updateAllDiscountForCart(cartCode, updateAllDiscountRequest);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/sale-off")
    public ResponseEntity<CartData> markEntrySaleOff(@PathVariable("cartCode") String cartCode,
                                                      @PathVariable("entryId") Long entryId,
                                                      @RequestBody EntrySaleOffRequest request) {
        request.setOrderCode(cartCode);
        request.setEntryId(entryId);
        CartData data = cartFacade.markEntrySaleOff(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{cartCode}/update-customer")
    public ResponseEntity<CartData> updateCustomer(@PathVariable("cartCode") String cartCode,
                                                   @RequestBody UpdateCustomerRequest request) {
        request.setCode(cartCode);
        CartData cartData = cartFacade.updateCustomer(request);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @PostMapping("/{cartCode}/add-vat")
    public ResponseEntity<CartData> addVAT(@PathVariable("cartCode") String cartCode,
                                                 @RequestParam("companyId") Long companyId,
                                                 @RequestParam("addVat") Boolean addVat) {
        CartData cartData = cartFacade.addVAT(companyId, cartCode, addVat);
        return new ResponseEntity<>(cartData, HttpStatus.OK);
    }

    @Autowired
    public void setRefreshCartRequestValidator(Validator<RefreshCartRequest> refreshCartRequestValidator) {
        this.refreshCartRequestValidator = refreshCartRequestValidator;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Autowired
    public void setAppliedCouponRequestValidator(Validator<AppliedCouponRequest> appliedCouponRequestValidator) {
        this.appliedCouponRequestValidator = appliedCouponRequestValidator;
    }

    @Autowired
    public void setAddSubOrderEntryRequestValidator(Validator<AddSubOrderEntryRequest> addSubOrderEntryRequestValidator) {
        this.addSubOrderEntryRequestValidator = addSubOrderEntryRequestValidator;
    }

    @Autowired
    public void setRemoveSubOrderEntryRequestValidator(Validator<RemoveSubOrderEntryRequest> removeSubOrderEntryRequestValidator) {
        this.removeSubOrderEntryRequestValidator = removeSubOrderEntryRequestValidator;
    }

    @Autowired
    public void setToppingOptionRequestValidator(Validator<ToppingOptionRequest> toppingOptionRequestValidator) {
        this.toppingOptionRequestValidator = toppingOptionRequestValidator;
    }

    @Autowired
    public void setToppingItemRequestValidator(Validator<ToppingItemRequest> toppingItemRequestValidator) {
        this.toppingItemRequestValidator = toppingItemRequestValidator;
    }
}
