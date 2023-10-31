package com.vctek.orderservice.service;

import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.migration.dto.InvoiceLinkDto;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface PaymentTransactionService {
    PaymentTransactionModel findById(Long id);

    PaymentTransactionModel save(PaymentTransactionModel model);

    void delete(PaymentTransactionModel existedModel);

    List<PaymentTransactionModel> findAll();

    List<PaymentTransactionModel> findAllByOrderCode(String orderCode);

    List<PaymentTransactionModel> findAllByReturnOrder(ReturnOrderModel returnOrder);

    List<PaymentTransactionModel>  saveAll(Collection<PaymentTransactionModel> paymentTransactions);

    List<PaymentTransactionData> findAllPaymentInvoiceOrder(OrderModel orderModel);

    List<PaymentTransactionData> findAllPaymentInvoiceReturnOrder(ReturnOrderModel returnOrderDocument);

    Page<PaymentTransactionModel> findAllForMigratePaymentMethod(Pageable pageable);

    List<PaymentTransactionModel> findPaymentForInvoiceLink(InvoiceLinkDto dto);

    PaymentTransactionModel findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId(Long moneySourceId, Long paymentMethodId, Long returnExternalId, Long companyId);

    void removePaymentByInvoice(OrderModel orderModel, Long invoiceId);

    PaymentTransactionModel findLoyaltyRedeem(OrderModel orderModel);

    void updatePaymentByInvoice(OrderModel model, InvoiceKafkaData invoiceData);

    void resetPaymentForLoyaltyRedeem(OrderModel model);
}
