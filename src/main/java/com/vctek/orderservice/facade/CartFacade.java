package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CartFacade {

    CartData getDetail(CartInfoParameter cartInfoParameter);

    CartData createNewCart(CartInfoParameter cartInfoParameter);

    void createNewImageInCart(OrderImagesRequest orderImages, String cardCode);

    void remove(CartInfoParameter cartInfoParameter);

    CartData addToCart(OrderEntryDTO orderEntryDTO);

    CartData refresh(RefreshCartRequest refreshCartRequest);

    CartData updateCartEntry(OrderEntryDTO orderEntryDTO);

    CartData updateDiscountOfCartEntry(OrderEntryDTO orderEntryDTO);

    CartData updateDiscountOfCart(CartDiscountRequest cartDiscountRequest);

    CartData updateVatOfCart(VatRequest vatRequest);

    CartData updatePriceCartEntry(OrderEntryDTO orderEntryDTO);

    CartData updateWeightForCartEntry(OrderEntryDTO orderEntryDTO);

    CartData applyCoupon(AppliedCouponRequest appliedCouponRequest);

    CartData removeCoupon(AppliedCouponRequest appliedCouponRequest);

    CartData addProductToCombo(AddSubOrderEntryRequest comboId);

    CartData addComboToOrderIndirectly(AddSubOrderEntryRequest request);

    void removeSubEntry(RemoveSubOrderEntryRequest request);

    <T extends AbstractOrderData> T importOrderItem(String cartCode, Long companyId, MultipartFile multipartFile);

    CartData appliedPromotion(String cartCode, Long companyId, Long promotionSourceRuleId);

    CartData addToppingOption(ToppingOptionRequest request, String cartCode);

    CartData addToppingItem(ToppingItemRequest request);

    CartData updateToppingOption(ToppingOptionRequest request, String cartCode);

    CartData updateToppingItem(ToppingItemRequest request);

    CartData updateDiscountForToppingItem(ToppingItemRequest request);

    CartData removeListCartEntry(EntryRequest request);

    AwardLoyaltyData getLoyaltyPointsFor(String cartCode, Long companyId);

    CartData updatePriceForCartEntries(CartInfoParameter cartInfoParameter);

    List<OrderSettingDiscountData> checkDiscountMaximum(Long companyId, String cartCode);

    CartData updateAllDiscountForCart(String cartCode, UpdateAllDiscountRequest updateAllDiscountRequest);

    CartData updateRecommendedRetailPriceForCartEntry(OrderEntryDTO orderEntryDTO);

    CartData markEntrySaleOff(EntrySaleOffRequest request);

    boolean isSaleOffEntry(OrderEntryDTO orderEntryDTO);

    CartData updateCustomer(UpdateCustomerRequest request);

    CartData addVAT(Long companyId, String cartCode, Boolean addVat);

    CartData changeOrderSource(CartInfoParameter cartInfoParameter);

}
