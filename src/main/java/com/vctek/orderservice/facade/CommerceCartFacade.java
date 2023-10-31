package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.CommerceCartData;
import com.vctek.orderservice.dto.CreateCartParam;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.dto.StorefrontOrderEntryDTO;
import com.vctek.orderservice.dto.request.AppliedCouponRequest;
import com.vctek.orderservice.dto.request.CommerceCancelOrderRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.storefront.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CommerceCartFacade {

    Map<Long, Double> calculateProductPromotionPrice(ProductPromotionRequest request);

    MiniCartData getMiniCart(Long companyId, String cartCode);

    CommerceCartData getOrCreateNewCart(CreateCartParam param);

    CommerceCartData addToCart(StorefrontOrderEntryDTO storefrontOrderEntryDTO);

    CommerceCartData updateCartEntry(StorefrontOrderEntryDTO orderEntryDTO);

    CommerceCartData getCartDetail(Long companyId, String cartCode);

    CommerceCartData changeProductInCombo(StoreFrontSubOrderEntryRequest subOrderEntryRequest);

    CommerceCartData applyCoupon(AppliedCouponRequest appliedCouponRequest);

    CommerceCartData removeCoupon(AppliedCouponRequest appliedCouponRequest);

    CommerceCartData placeOrder(StoreFrontCheckoutRequest request);

    CommerceCartData appliedPromotion(String cartCode, Long companyId, Long promotionSourceRuleId);

    Page<CommerceOrderData> getOrderByUser(OrderSearchRequest request, Pageable page);

    void cancelOrder(CommerceCancelOrderRequest request);

    void resetChangeGift(String cartCode, Long companyId);

    CommerceCartData getDetailOrder(String orderCode, Long companyId);

    List<CountOrderData> countOrderByUser(Long companyId);

    CommerceCartData updateAddressShipping(StoreFrontCheckoutRequest request);
}
