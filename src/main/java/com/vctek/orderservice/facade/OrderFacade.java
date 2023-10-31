package com.vctek.orderservice.facade;

import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface OrderFacade {
    OrderData placeOrder(OrderRequest orderRequest);

    OrderData findBy(String orderCode, Long companyId, String orderType, boolean isExchange);

    OrderData addEntryToOrder(OrderEntryDTO orderEntryDTO);

    OrderData updateEntry(OrderEntryDTO orderEntryDTO);

    OrderData updateDiscountOfEntry(OrderEntryDTO orderEntryDTO);

    OrderData updateDiscountOfOrder(CartDiscountRequest cartDiscountRequest);

    OrderData updateVatOfOrder(VatRequest vatRequest);

    OrderData updateOrderInfo(OrderRequest orderRequest);

    OrderData updateInfoOnlineOrder(OrderRequest orderRequest);

    OrderData updatePriceOrderEntry(OrderEntryDTO orderEntryDTO);

    void changeStatusOrder(ChangeOrderStatusRequest request);

    String changeOrderToRetail(String orderCode, Long companyId);

    void holdingProductOfOrder(HoldingProductRequest request);

    void remove(String orderCode, Long companyId);

    void updateWeightForOrderEntry(OrderEntryDTO orderEntryDTO);

    void updateNoteInOrder(NoteRequest noteRequest);

    void updateHoldingProductBy(String orderCode, Long entryId, HoldingData holdingData);

    OrderData applyCoupon(AppliedCouponRequest appliedCouponRequest);

    OrderData removeCoupon(AppliedCouponRequest appliedCouponRequest);

    OrderData addProductToCombo(AddSubOrderEntryRequest request);

    OrderData addComboToOrderIndirectly(AddSubOrderEntryRequest request);

    void removeSubEntry(RemoveSubOrderEntryRequest request);

    <T extends AbstractOrderData> T importOrderItem(String orderCode, Long companyId, MultipartFile multipartFile);

    Map<Long, OrderSaleData> getSaleQuantity(SaleQuantityRequest request);

    OrderData appliedPromotion(String orderCode, Long companyId, Long promotionSourceRuleId);

    OrderData updateToppingOption(ToppingOptionRequest request, String orderCode);

    OrderData addToppingOptionsToOrder(ToppingOptionRequest request, String orderCode);

    OrderData addToppingItems(String orderCode, ToppingItemRequest request);

    OrderData removeToppingOptions(String orderCode, Long entryId, Long toppingOptionId, Long companyId);

    OrderData removeToppingItems(String orderCode, Long entryId, Long optionId, Long toppingItemId, Long companyId);

    OrderData updateQuantityToppingItems(ToppingItemRequest request, String orderCode);

    OrderData updateDiscountForToppingItem(ToppingItemRequest request);

    void createOrUpdateInvoices(InvoiceOrderRequest invoiceOrderRequest);

    void updatePaymentTransactionDataAndPaidAmount(InvoiceKafkaData invoiceKafkaData);

    void updatePaidAmountAllOrder(OrderPartialIndexRequest request);

    OrderData refresh(RefreshCartRequest refreshCartRequest);

    void updateComboReport(Long companyId);

    OrderData removeListEntry(EntryRequest request);

    void linkOrderToBill(Long companyId);

    OrderData updatePriceForOrderEntries(OrderRequest orderRequest);

    void updateOrderSourceForReturnOrder(com.vctek.kafka.data.OrderData content);

    OrderData updateShippingFee(OrderRequest orderRequest);

    OrderData updateDefaultSettingCustomer(OrderRequest orderRequest);

    List<OrderSettingDiscountData> checkDiscountMaximum(Long companyId, String orderCode);

    OrderData updateAllDiscountForOrder(String orderCode, UpdateAllDiscountRequest updateAllDiscountRequest);

    OrderData findOrderByExternalId(CartInfoParameter cartInfoParameter);

    void uploadImageToOrder(OrderImagesRequest orderImages, String orderCode);

    OrderData updateRecommendedRetailPriceForOrderEntry(OrderEntryDTO orderEntryDTO);

    OrderData cancelRedeem(String orderCode, Long companyId);

    double updateRedeemOnline(String orderCode, Long companyId, PaymentTransactionRequest request);

    double createRedeemOnline(String orderCode, Long companyId, PaymentTransactionRequest request);

    AwardLoyaltyData getLoyaltyPointsFor(String orderCode, Long companyId);

    OrderData updateSettingCustomerToOrder(OrderRequest orderRequest);

    void addTag(AddTagRequest addTagRequest);

    void removeTag(Long companyId, String orderCode, Long tagId);

    OrderData markEntrySaleOff(EntrySaleOffRequest request);

    boolean isSaleOffEntry(OrderEntryDTO orderEntryDTO);

    OrderData updateCustomer(UpdateCustomerRequest request);

    OrderData addVAT(Long companyId, String orderCode, Boolean addVat);

    OrderData changeOrderSource(CartInfoParameter cartInfoParameter);
}
