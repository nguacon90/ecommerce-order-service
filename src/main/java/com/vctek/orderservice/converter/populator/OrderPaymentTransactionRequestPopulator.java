package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.FinanceService;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.util.PaymentMethodType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component("orderPaymentTransactionRequestPopulator")
public class OrderPaymentTransactionRequestPopulator implements Populator<List<PaymentTransactionRequest>, OrderModel> {

    private FinanceService financeService;
    private LoyaltyService loyaltyService;

    public OrderPaymentTransactionRequestPopulator(FinanceService financeService, LoyaltyService loyaltyService) {
        this.financeService = financeService;
        this.loyaltyService = loyaltyService;
    }
    @Override
    public void populate(List<PaymentTransactionRequest> payments, OrderModel order) {
        Set<PaymentTransactionModel> transactions = order.getPaymentTransactions();
        Set<PaymentTransactionModel> updateTransactions = new HashSet<>();
        updateTransactions.addAll(transactions);
        PaymentMethodData loyaltyPaymentData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        for(PaymentTransactionRequest request : payments) {
            PaymentTransactionModel transaction = findPaymentBy(transactions, request);
            transaction.setAmount(request.getAmount());
            transaction.setMoneySourceId(request.getMoneySourceId());
            transaction.setPaymentMethodId(request.getPaymentMethodId());
            transaction.setTransactionNumber(request.getTransactionNumber());
            transaction.setMoneySourceType(request.getType());
            transaction.setWarehouseId(order.getWarehouseId());
            if (loyaltyPaymentData.getId().equals(request.getPaymentMethodId()) && StringUtils.isNotBlank(order.getCardNumber())) {
                LoyaltyCardData loyaltyCardData = loyaltyService.findByCardNumber(order.getCardNumber(),
                        order.getCompanyId());
                transaction.setConversionRate(loyaltyCardData.getConversionRate());
            }
            if(transaction.getId() == null && transaction.getAmount() != null && transaction.getAmount() > 0) {
                transaction.setOrderCode(order.getCode());
                transaction.setOrderModel(order);
                updateTransactions.add(transaction);
            }
        }

        order.getPaymentTransactions().addAll(updateTransactions);
    }

    private PaymentTransactionModel findPaymentBy(Set<PaymentTransactionModel> paymentTransactions, PaymentTransactionRequest request) {
        Optional<PaymentTransactionModel> first = paymentTransactions.stream().filter(p -> p.getId().equals(request.getId())).findFirst();
        if(first.isPresent()) {
            return first.get();
        }

        Optional<PaymentTransactionModel> modelOptional = paymentTransactions.stream()
                .filter(p -> p.getMoneySourceId() != null && p.getPaymentMethodId() != null)
                .filter(p -> p.getMoneySourceId().equals(request.getMoneySourceId()) && p.getPaymentMethodId().equals(request.getPaymentMethodId()))
                .findFirst();
        if(modelOptional.isPresent()) {
            return modelOptional.get();
        }

        return new PaymentTransactionModel();
    }
}
