package com.vctek.orderservice.service;

import com.vctek.orderservice.model.LoyaltyTransactionModel;

import java.util.List;

public interface LoyaltyTransactionService {
    LoyaltyTransactionModel save(LoyaltyTransactionModel loyaltyTransactionModel);

    LoyaltyTransactionModel findByOrderCodeAndInvoiceNumber(String orderCode, String invoiceNumber);

    List<LoyaltyTransactionModel> findByAllOrderCode(String orderCode);

    LoyaltyTransactionModel findLastByOrderCode(String code);

    LoyaltyTransactionModel findLastByOrderCodeAndListType(String code, List<String> types);

    void cloneAwardRedeemLoyaltyTransaction(String orderCode, String retailOrderCode);
}
