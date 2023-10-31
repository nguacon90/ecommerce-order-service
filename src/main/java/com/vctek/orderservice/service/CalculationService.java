package com.vctek.orderservice.service;


import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.OrderModel;

import java.util.List;

public interface CalculationService {
    void calculateTotals(AbstractOrderModel order, boolean recalculate);

    void calculate(AbstractOrderModel order);

    void recalculate(AbstractOrderModel order);

    void calculateVat(AbstractOrderModel order);

    double calculateFinalDiscountOfEntry(AbstractOrderEntryModel entry);

    double totalDiscountTopping(AbstractOrderModel orderModel);

    double calculateLoyaltyAmount(List<ProductCanRewardDto> productCanRewardDtoList, Long companyId);

    AbstractOrderModel saveRewardAmountToEntries(AbstractOrderModel orderModel, double point, double loyaltyAmount, List<ProductCanRewardDto> canRewardDto, Boolean isUpdateOrder);

    double calculateMaxRevertAmount(ReturnOrderRequest returnOrderRequest, OrderModel originOrder);

    double calculateMaxRefundAmount(ReturnOrderCommerceParameter commerceParameter);

    double calculateRemainCashAmount(ReturnOrderCommerceParameter commerceParameter);

    double round(Double value, int places);

    void calculateSubEntryPriceWithCombo(AbstractOrderEntryModel entry);

    void clearComboEntryPrices(CartEntryModel entryModel);

    void calculateVatByProductOf(AbstractOrderModel abstractOrderModel, boolean recalculate);

    void resetVatOf(AbstractOrderModel abstractOrderModel);
}
