package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.migration.dto.InvoiceLinkDto;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.repository.PaymentTransactionRepository;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.PaymentTransactionService;
import com.vctek.util.BillStatus;
import com.vctek.util.OrderType;
import com.vctek.util.PaymentMethodType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentTransactionServiceImpl implements PaymentTransactionService {
    private PaymentTransactionRepository paymentTransactionRepository;
    private FinanceClient financeClient;
    private AuthService authService;

    public PaymentTransactionServiceImpl(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    public PaymentTransactionModel findById(Long id) {
        Optional<PaymentTransactionModel> paymentMethodModel = paymentTransactionRepository.findById(id);
        return paymentMethodModel.isPresent() ? paymentMethodModel.get() : null;
    }

    @Override
    public PaymentTransactionModel save(PaymentTransactionModel model) {
        return paymentTransactionRepository.save(model);
    }

    @Override
    public void delete(PaymentTransactionModel existedModel) {
        paymentTransactionRepository.delete(existedModel);
    }

    @Override
    public List<PaymentTransactionModel> findAll() {
        return paymentTransactionRepository.findAll();
    }

    @Override
    public List<PaymentTransactionModel> findAllByOrderCode(String orderCode) {
        return paymentTransactionRepository.findAllByOrderCode(orderCode);
    }

    @Override
    public List<PaymentTransactionModel> findAllByReturnOrder(ReturnOrderModel returnOrder) {
        return paymentTransactionRepository.findAllByReturnOrder(returnOrder);
    }

    @Override
    public List<PaymentTransactionModel> saveAll(Collection<PaymentTransactionModel> paymentTransactions) {
        return paymentTransactionRepository.saveAll(paymentTransactions);
    }

    @Override
    public List<PaymentTransactionData> findAllPaymentInvoiceOrder(OrderModel orderModel) {
        if (authService.isCurrentCustomerUserOrAnonymous()) {
            return new ArrayList<>();
        }

        List<InvoiceData> invoiceData = financeClient.findAllOrderInvoices(orderModel.getCompanyId(), orderModel.getCode(), null, "");
        if (CollectionUtils.isEmpty(invoiceData)) {
            return new ArrayList<>();
        }
        return populatePaymentTransactionData(invoiceData);
    }

    @Override
    public List<PaymentTransactionData> findAllPaymentInvoiceReturnOrder(ReturnOrderModel returnOrderDocument) {
        List<InvoiceData> invoiceData = financeClient.findAllOrderInvoices(returnOrderDocument.getCompanyId(), null, returnOrderDocument.getId(), OrderType.RETURN_ORDER.toString());
        if (CollectionUtils.isEmpty(invoiceData)) {
            return new ArrayList<>();
        }
        return populatePaymentTransactionData(invoiceData);
    }

    @Override
    public Page<PaymentTransactionModel> findAllForMigratePaymentMethod(Pageable pageable) {
        return paymentTransactionRepository.findAllByInvoiceIdIsNotNull(pageable);
    }

    @Override
    public List<PaymentTransactionModel> findPaymentForInvoiceLink(InvoiceLinkDto dto) {
        return paymentTransactionRepository.findByMoneySourceIdAndPaymentMethodIdAndOrderCode(dto.getMoneySourceId(), dto.getPaymentMethodId(), dto.getOrderCode());
    }

    @Override
    public PaymentTransactionModel findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId(Long moneySourceId, Long paymentMethodId, Long returnExternalId, Long companyId) {
        return paymentTransactionRepository.findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId(moneySourceId, paymentMethodId, returnExternalId, companyId);
    }

    @Override
    public void removePaymentByInvoice(OrderModel orderModel, Long invoiceId) {
        if(!OrderType.ONLINE.toString().equalsIgnoreCase(orderModel.getType())) {
            return;
        }

        List<PaymentTransactionModel> payments = paymentTransactionRepository.findAllByOrderModelAndInvoiceId(orderModel, invoiceId);
        if(CollectionUtils.isNotEmpty(payments)) {
            paymentTransactionRepository.deleteAll(payments);
        }
    }

    @Override
    public PaymentTransactionModel findLoyaltyRedeem(OrderModel orderModel) {
        List<PaymentTransactionModel> paymentTransactions = this.findAllByOrderCode(orderModel.getCode());
        if(CollectionUtils.isEmpty(paymentTransactions)) {
            return null;
        }

        PaymentMethodData loyaltyPaymentMethodData = financeClient.getPaymentMethodDataByCode(PaymentMethodType.LOYALTY_POINT.code());
        Optional<PaymentTransactionModel> redeemPaymentTransactionOptional = paymentTransactions.stream().
                filter(p -> p.isDeleted() == false && loyaltyPaymentMethodData.getId().equals(p.getPaymentMethodId())).findFirst();
        if(!redeemPaymentTransactionOptional.isPresent()) {
            return null;
        }

        return redeemPaymentTransactionOptional.get();
    }

    private List<PaymentTransactionData> populatePaymentTransactionData(List<InvoiceData> invoiceOrderData) {
        List<PaymentTransactionData> dataList = new ArrayList<>();
        invoiceOrderData.forEach(invoice -> {
            if (BillStatus.VERIFIED.code().equals(invoice.getStatus())) {
                PaymentTransactionData paymentTransactionData = new PaymentTransactionData();
                paymentTransactionData.setInvoiceId(invoice.getId());
                paymentTransactionData.setAmount(invoice.getFinalAmount());
                if (invoice.getMoneySource() != null) {
                    paymentTransactionData.setMoneySourceId(invoice.getMoneySource().getId());
                    paymentTransactionData.setMoneySourceType(invoice.getMoneySource().getType());
                }
                if (invoice.getPaymentMethod() != null) {
                    paymentTransactionData.setPaymentMethodId(invoice.getPaymentMethod().getId());
                }

                dataList.add(paymentTransactionData);
            }
        });


        return dataList;
    }

    @Override
    public void updatePaymentByInvoice(OrderModel model, InvoiceKafkaData invoiceData) {
        if(!OrderType.ONLINE.toString().equalsIgnoreCase(model.getType())) {
            return;
        }

        List<PaymentTransactionModel> payments = paymentTransactionRepository.findByMoneySourceIdAndPaymentMethodIdAndOrderCode(
                invoiceData.getMoneySourceId(), invoiceData.getPaymentMethodId(), model.getCode());
        boolean deleted = false;
        if (!BillStatus.VERIFIED.code().equals(invoiceData.getStatus())) {
            deleted = true;
        }
        if (CollectionUtils.isEmpty(payments)) return;

        for (PaymentTransactionModel payment : payments) {
            if (!payment.isDeleted()) {
                payment.setDeleted(deleted);
            }

            //invoice status = VERIFIED and payment invoice id = null
            if (!payment.isDeleted() && payment.getInvoiceId() == null) {
                payment.setInvoiceId(invoiceData.getInvoiceId());
            }
        }
        paymentTransactionRepository.saveAll(payments);
    }

    @Override
    @Transactional
    public void resetPaymentForLoyaltyRedeem(OrderModel model) {
        List<PaymentTransactionModel> paymentTransactions = this.findAllByOrderCode(model.getCode());
        if(CollectionUtils.isEmpty(paymentTransactions)) {
            return;
        }
        PaymentMethodData paymentMethodData = financeClient.getPaymentMethodDataByCode(PaymentMethodType.LOYALTY_POINT.code());
        for (PaymentTransactionModel payment : paymentTransactions) {
            if (!payment.isDeleted() && paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
                payment.setDeleted(true);
            }
        }
        saveAll(paymentTransactions);
    }

    @Autowired
    public void setFinanceClient(FinanceClient financeClient) {
        this.financeClient = financeClient;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
