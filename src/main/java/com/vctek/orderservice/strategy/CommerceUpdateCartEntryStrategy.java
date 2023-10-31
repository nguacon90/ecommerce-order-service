package com.vctek.orderservice.strategy;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.request.EntryRequest;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ToppingOptionModel;

import java.util.List;

public interface CommerceUpdateCartEntryStrategy {
    CommerceCartModification updateQuantityForCartEntry(CommerceAbstractOrderParameter parameter);

    void updateDiscountForCartEntry(CommerceAbstractOrderParameter parameter);

    void updatePriceForCartEntry(CommerceAbstractOrderParameter parameter);

    void updateWeightForOrderEntry(CommerceAbstractOrderParameter parameter);

    void updateSubOrderEntry(AbstractOrderEntryModel order);

    CommerceCartModification removeListCartEntry(AbstractOrderModel model, EntryRequest request);

    void validatePriceForCartEntry(AbstractOrderEntryModel orderEntryModel, AbstractOrderModel model);

    boolean updateRecommendedRetailPriceForCartEntry(CommerceAbstractOrderParameter parameter);

    void updateSubOrderEntryQty(AbstractOrderEntryModel orderEntry, int entryOldQuantity, int entryNewQuantity);

    void updateBasePriceForComboIfNeed(AbstractOrderModel model, AbstractOrderEntryModel entry, int entryQty);

    AbstractOrderEntryModel markEntrySaleOff(CommerceAbstractOrderParameter parameter);

    void handleUpdateEntryStockHoldingOnline(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel entryToUpdate, long actualAllowedQuantityChange);

    void addOrRemoveStockHoldingToppingWithOrder(OrderModel orderModel, List<ToppingOptionModel> toppingOptionModels, boolean deleted);

    void updateStockHoldingToppingOptionWithOrder(OrderModel orderModel, ToppingOptionModel toppingOptionModel, int optionQty);

    void handleUpdateToppingItemStockHoldingOnline(OrderModel orderModel, Long productId, Integer quantity);

}
