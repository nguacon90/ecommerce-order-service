package com.vctek.orderservice.service;

import com.vctek.dto.request.CheckValidCardParameter;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.AwardLoyaltyData;
import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.LoyaltyTransactionModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;

import java.util.List;

public interface LoyaltyService {

    void assignCardToCustomerIfNeed(String cardNumber, CustomerRequest customerRequest, boolean isExchange, Long warehouseId);

    boolean isValid(CheckValidCardParameter parameter);

    boolean isApplied(OrderModel orderModel);

    List<ProductCanRewardDto> getAwardProducts(AbstractOrderModel model);

    void reward(OrderModel orderModel);

    TransactionData callReward(OrderModel orderModel, Double awardMount);

    LoyaltyCardData findByCardNumber(String cardNumber, Long companyId);

    AvailablePointAmountData computeAvailablePointAmountOf(AvailablePointAmountRequest request);

    TransactionData redeem(OrderModel orderModel, Double amount);

    TransactionData refund(OrderModel orderModel, ReturnOrderModel returnOrderModel, Double amount);

    TransactionData revert(OrderModel orderModel, ReturnOrderModel returnOrderModel, Double amount);

    TransactionData updateReward(OrderModel orderModel);

    TransactionData updateRedeem(OrderModel orderModel);

    OrderModel recalculateRewardAmount(OrderModel orderModel);

    TransactionData updateRefund(ReturnOrderModel returnOrderModel);

    void updateRewardRedeemForOrder(OrderModel orderToUpdate);

    TransactionData findByInvoiceNumberAndCompanyIdAndType(TransactionRequest transactionRequest);

    AwardLoyaltyData getLoyaltyPointsOf(AbstractOrderModel cartModel);

    LoyaltyTransactionModel createLoyaltyTransaction(String orderCode, String invoiceNumber, String transactionType, Double conversionRate, Long returnOrderId);

    double convertAmountToPoint(double amount, Long companyId);

    void splitRewardAmountToEntriesAndCreateLoyaltyTransaction(TransactionData transactionData);

    TransactionData createRedeemPending(OrderModel orderModel, Double amount);

    void cancelPendingRedeem(OrderModel model);

    void cancelPendingRedeemForCancelOrder(OrderModel model);

    TransactionData updatePendingRedeem(OrderModel model, PaymentTransactionRequest request);

    void completeRedeemLoyaltyForOnline(OrderModel order);

    void revertOnlineOrderReward(OrderModel order, Double totalRewardAmount);

    TransactionRequest populateRedeemKafkaForOnlineOrder(OrderModel orderModel, Double amount);

    TransactionRequest populateRefundKafkaForOnlineOrder(OrderModel orderModel, Double amount);

    TransactionRequest populateRevertKafkaForOnlineOrder(OrderModel orderModel, Double amount);

    TransactionRequest populateCompleteRedeemKafkaForOnlineOrder(OrderModel orderModel);

    TransactionRequest populateCancelRedeemKafkaForOnlineOrder(OrderModel orderModel);

    void resetPaymentForLoyalty(OrderModel model);
}
