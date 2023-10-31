package com.vctek.orderservice.strategy;

import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.CommerceCheckoutParameter;
import com.vctek.orderservice.dto.CommerceOrderResult;
import com.vctek.orderservice.dto.UpdateOrderParameter;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.feignclient.dto.OrderBillRequest;
import com.vctek.orderservice.model.*;

import java.util.List;

public interface CommercePlaceOrderStrategy {
    CommerceOrderResult placeOrder(CommerceCheckoutParameter checkoutParameter);

    void updateProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification);

    void deleteProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification);

    void deleteProductOfComboInReturnBillWithOrder(OrderModel order, OrderEntryModel entryModel, SubOrderEntryModel subOrderEntryModel);

    CommerceOrderResult updateOrder(UpdateOrderParameter updateOrderParameter);

    CommerceOrderResult updateCustomerInfoInOnlineOrder(UpdateOrderParameter updateOrderParameter);

    OrderModel changeBillToRetail(OrderModel order);

    void updatePriceAndDiscountBillOf(OrderModel order);

    void updateComboInReturnBillWithOrder(OrderModel order, AbstractOrderEntryModel entry);

    void updateOrDeleteToppingInReturnBillWithOrder(OrderModel order, List<OrderBillRequest> orderBillRequests);

    void addProductToReturnBill(OrderModel order, AbstractOrderEntryModel abstractOrderEntry);

    void revertComboSaleQuantity(Long companyId, OrderEntryModel entryModel);

    OrderModel cancelRedeem(OrderModel model);

    double updateRedeemOnline(OrderModel model, PaymentTransactionRequest request);

    double createRedeemOnline(OrderModel model, PaymentTransactionRequest request);

    OrderModel changeBillToRetailForKafkaImportOrderStatus(OrderModel order);

    OrderModel updateSettingCustomerToOrder(OrderModel orderModel, List<Long> settingCustomerOptionIds);

    OrderModel storefrontPlaceOrder(CommerceCheckoutParameter checkoutParameter);

    OrderModel updateAddressShipping(OrderModel orderModel, ShippingFeeData shippingFeeData, StoreFrontCheckoutRequest request);
}
