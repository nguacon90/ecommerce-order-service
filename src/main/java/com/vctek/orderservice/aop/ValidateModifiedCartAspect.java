package com.vctek.orderservice.aop;

import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CartService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ValidateModifiedCartAspect extends AbstractValidateAspect {
    private CartService cartService;

    @Before("execution(* com.vctek.orderservice.controller.CartController.addCartEntry(..)) && args(cartCode, orderEntryDTO)")
    public void validateAddCartEntry(String cartCode, OrderEntryDTO orderEntryDTO) {
        if (orderEntryDTO == null) {
            return;
        }
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, orderEntryDTO.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.removeCartEntry(..)) && args(cartCode, entryId, companyId, timeRequest)")
    public void validateRemoveCartEntry(String cartCode, Long entryId, Long companyId, Long timeRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, companyId);
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.updateDiscountForCart(..)) && args(cartCode, cartDiscountRequest, timeRequest)")
    public void validateUpdateDiscountForCart(String cartCode, CartDiscountRequest cartDiscountRequest, Long timeRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, cartDiscountRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.updateQuantityForCartEntry(..)) && args(cartCode, entryId, orderEntryDTO, timeRequest)" +
            "|| execution(* com.vctek.orderservice.controller.CartController.updatePriceForCartEntry(..)) && args(cartCode, entryId, orderEntryDTO, timeRequest)" +
            "|| execution(* com.vctek.orderservice.controller.CartController.updateDiscountForCartEntry(..)) && args(cartCode, entryId, orderEntryDTO, timeRequest)")
    public void validateUpdateQuantityForCartEntry(String cartCode, Long entryId, OrderEntryDTO orderEntryDTO, Long timeRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, orderEntryDTO.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.updateVatForCart(..)) && args(cartCode, vatRequest, timeRequest)")
    public void validateUpdateVatForCart(String cartCode, VatRequest vatRequest, Long timeRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, vatRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.updateWeightForCartEntry(..)) && args(cartCode, entryId, orderEntryDTO)")
    public void validateUpdateWeightForCartEntry(String cartCode, Long entryId, OrderEntryDTO orderEntryDTO) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, orderEntryDTO.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.applyCoupon(..)) && args(cartCode, appliedCouponRequest)" +
            "|| execution(* com.vctek.orderservice.controller.CartController.removeCoupon(..)) && args(cartCode, appliedCouponRequest)")
    public void validateApplyCoupon(String cartCode, AppliedCouponRequest appliedCouponRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, appliedCouponRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.addProductToCombo(..)) && args(cartCode, entryId, addSubOrderEntryRequest)" +
            "|| execution(* com.vctek.orderservice.controller.CartController.addComboToOrderIndirectly(..)) && args(cartCode, entryId, addSubOrderEntryRequest)")
    public void validateAddProductToCombo(String cartCode, Long entryId, AddSubOrderEntryRequest addSubOrderEntryRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, addSubOrderEntryRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.removeSubEntry(..)) && args(cartCode, entryId, removeSubOrderEntryRequest)")
    public void validateRemoveSubEntry(String cartCode, Long entryId, RemoveSubOrderEntryRequest removeSubOrderEntryRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, removeSubOrderEntryRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.appliedPromotion(..)) && args(cartCode, companyId, promotionSourceRuleId)")
    public void validateAppliedPromotion(String cartCode, Long companyId, Long promotionSourceRuleId) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, companyId);
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.addToppingOptionToCart(..)) && args(cartCode, entryId, toppingOptionRequest)")
    public void validateAddToppingOption(String cartCode, Long entryId, ToppingOptionRequest toppingOptionRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, toppingOptionRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.updateToppingOption(..)) && args(cartCode, entryId, optionId, toppingOptionRequest, timeRequest)")
    public void validateUpdateToppingOption(String cartCode, Long entryId, Long optionId, ToppingOptionRequest toppingOptionRequest, Long timeRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, toppingOptionRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.removeToppingOption(..)) && args(cartCode, entryId, optionId, companyId)")
    public void validateRemoveToppingOption(String cartCode, Long entryId, Long optionId, Long companyId) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, companyId);
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.addToppingItem(..)) && args(cartCode, optionId, toppingItemRequest, entryId)")
    public void validateAddToppingItem(String cartCode, Long optionId, ToppingItemRequest toppingItemRequest, Long entryId) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, toppingItemRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.updateToppingItems(..)) && args(cartCode, optionId, entryId, itemId, toppingItemRequest, timeRequest)" +
            "|| execution(* com.vctek.orderservice.controller.CartController.updateDiscountForToppingItem(..)) && args(cartCode, optionId, entryId, itemId, toppingItemRequest, timeRequest)")
    public void validateUpdateToppingItem(String cartCode, Long optionId, Long entryId, Long itemId, ToppingItemRequest toppingItemRequest, Long timeRequest) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, toppingItemRequest.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Before("execution(* com.vctek.orderservice.controller.CartController.updatePriceForCartEntries(..)) && args(cartCode, cartInfoParameter)")
    public void validateUpdatePriceType(String cartCode, CartInfoParameter cartInfoParameter) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, cartInfoParameter.getCompanyId());
        validateActiveWarehouseOf(cartModel);
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
