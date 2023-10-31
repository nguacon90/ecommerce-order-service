package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.dto.request.storefront.ProductPromotionRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontSubOrderEntryRequest;
import com.vctek.orderservice.model.*;

import java.util.List;
import java.util.Map;

public interface CommerceCartService {

    CommerceCartModification addToCart(final CommerceAbstractOrderParameter parameter);

    CommerceCartModification addEntryToOrder(final CommerceAbstractOrderParameter parameter);

    void removeAllEntries(final CommerceAbstractOrderParameter parameter);

    CommerceCartModification updateQuantityForCartEntry(CommerceAbstractOrderParameter parameter);

    void updateDiscountForCartEntry(CommerceAbstractOrderParameter parameter);

    void updateDiscountForOrderEntry(CommerceAbstractOrderParameter parameter);

    void updateDiscountForCart(CommerceAbstractOrderParameter parameter);

    void updateDiscountForOrder(CommerceAbstractOrderParameter parameter);

    void updateVatForCart(CommerceAbstractOrderParameter parameter);

    void updatePriceForCartEntry(CommerceAbstractOrderParameter parameter);

    void updatePriceForCartEntries(AbstractOrderModel orderModel);

    void recalculate(AbstractOrderModel abstractOrderModel, boolean recalculateVat);

    void updateWeightForOrderEntry(CommerceAbstractOrderParameter parameter);

    void changeOrderEntryToComboEntry(CommerceAbstractOrderParameter cartParameter);

    void updateSubOrderEntry(AbstractOrderEntryModel order);

    CommerceCartModification updateOrderEntry(CommerceAbstractOrderParameter parameter);

    boolean shouldUpdateOrderBill(OrderModel orderModel);

    CommerceCartModification addProductToCombo(CommerceAbstractOrderEntryParameter parameter);

    void addToppingItem(ToppingItemParameter parameter);

    void addToppingOption(ToppingOptionParameter parameter);

    ToppingOptionModification updateToppingOption(ToppingOptionParameter parameter);

    ToppingItemModification updateToppingItem(ToppingItemParameter parameter);

    void deleteToppingOptionInOrder(ToppingOptionParameter parameter);

    void updateOrderToppingOption(ToppingOptionParameter parameter);

    void updateOrderToppingItem(ToppingItemParameter parameter);

    CommerceCartModification addProductToComboInOrder(CommerceAbstractOrderEntryParameter parameter);

    void updateHolingProduct(OrderModel order, OrderEntryModel entryModel, HoldingData holdingData);

    void updateNoteInOrder(OrderModel order, NoteRequest noteRequest);

    void removeOrder(OrderModel ordermodel);

    void holdingProductOfOrder(HoldingProductRequest request, OrderModel orderModel);

    void updateDiscountForToppingItem(ToppingItemParameter parameter);

    void updateListOrderEntry(AbstractOrderModel model, EntryRequest request);

    void calculateComboEntryPrices(CartEntryModel entryModel);

    void clearComboEntryPrices(CartEntryModel entryModel);

    void updateShippingFee(CommerceAbstractOrderParameter parameter);

    void updateDefaultSettingCustomer(CommerceAbstractOrderParameter parameter);

    List<OrderSettingDiscountData> checkDiscountMaximumOrder(AbstractOrderModel abstractOrderModel);

    Map<Long, OrderSettingDiscountData> checkDiscountMaximumProduct(AbstractOrderModel abstractOrderModel, Long productId);

    void updateAllDiscountForCart(CommerceAbstractOrderParameter parameter, UpdateAllDiscountRequest request);

    boolean updateRecommendedRetailPriceForCartEntry(CommerceAbstractOrderParameter parameter);

    void markEntrySaleOff(CommerceAbstractOrderParameter parameter);

    AbstractOrderModel updateCustomer(UpdateCustomerRequest request, AbstractOrderModel abstractOrderModel);

    AbstractOrderModel addVatOf(AbstractOrderModel cart, Boolean addVat);

    Map<Long, Double> getDiscountPriceFor(ProductPromotionRequest request);

    CartModel getStorefrontCart(String cartCode, Long companyId);

    CartModel getOrCreateNewStorefrontCart(CartInfoParameter parameter);

    CartModel getByCompanyIdAndGuid(Long companyId, String guid);

    void mergeCarts(CartModel fromCart, CartModel toCart);

    AbstractOrderEntryModel getExistedEntry(CommerceAbstractOrderParameter cartParameter);

    CommerceCartModification updateLatestPriceForEntries(CartModel cartModel);

    CommerceCartValidateData validate(CommerceCartValidateParam param);

    CartModel changeProductInCombo(StoreFrontSubOrderEntryRequest subOrderEntryRequest);

    AbstractOrderModel changeOrderSource(AbstractOrderModel abstractOrderModel, Long orderSourceId);

}
