package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.PaymentInvoiceData;
import com.vctek.kafka.data.loyalty.LoyaltyInvoiceData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.feignclient.dto.InvoiceOrderData;
import com.vctek.orderservice.feignclient.dto.InvoiceRequest;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.FinanceService;
import com.vctek.orderservice.service.InvoiceService;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.service.PaymentTransactionService;
import com.vctek.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceServiceImpl.class);
    private FinanceService financeService;
    private PaymentTransactionService paymentTransactionService;
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;
    private OrderHistoryService orderHistoryService;

    @Override
    @Transactional
    public void saveInvoices(OrderModel order, Long customerId) {

        Set<PaymentTransactionModel> paymentTransactions = order.getPaymentTransactions();
        if (CollectionUtils.isEmpty(paymentTransactions)) {
            LOGGER.warn("Empty payment transactions of order: {}", order.getCode());
            return;
        }

        List<InvoiceRequest> invoiceRequests = new ArrayList<>();
        Double finalPrice = order.getFinalPrice();

        Set<PaymentTransactionModel> otherPaymentMethods = paymentTransactions.stream()
                .filter(p -> !MoneySourceType.CASH.toString().equals(p.getMoneySourceType()))
                .collect(Collectors.toSet());

        Optional<PaymentTransactionModel> cashPaymentTransaction = paymentTransactions.stream()
                .filter(p -> MoneySourceType.CASH.toString().equals(p.getMoneySourceType()))
                .findFirst();

        if (CollectionUtils.isEmpty(otherPaymentMethods) && !cashPaymentTransaction.isPresent()) {
            LOGGER.warn("Order has not payment transactions: {}", order.getCode());
            return;
        }

        for (PaymentTransactionModel payment : otherPaymentMethods) {
            Double paymentAmount = payment.getAmount();
            if ((finalPrice == 0 && payment.getAmount() == null) || isPendingRedeemLoyaltyPointForOnline(order, payment)) {
                continue;
            }

            InvoiceRequest request = populateInvoiceRequest(order, customerId, payment, paymentAmount);
            invoiceRequests.add(request);
            finalPrice -= paymentAmount;
        }

        InvoiceRequest cashInvoice = populateCashAmount(order, customerId, finalPrice, cashPaymentTransaction);
        if (cashInvoice != null) {
            invoiceRequests.add(cashInvoice);
        }

        if (CollectionUtils.isEmpty(invoiceRequests)) {
            return;
        }

        try {
            InvoiceOrderData invoiceOrderData = financeService.createInvoiceOrder(invoiceRequests);
            saveInvoiceToPayments(paymentTransactions, invoiceOrderData);

        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }


    }

    private boolean isPendingRedeemLoyaltyPointForOnline(OrderModel order, PaymentTransactionModel payment) {
        if(!OrderType.ONLINE.toString().equals(order.getType())) {
            return false;
        }

        if(OrderStatus.COMPLETED.code().equals(order.getOrderStatus())) {
            return false;
        }

        return isRedeemLoyaltyPayment(payment);
    }

    private boolean isRedeemLoyaltyPayment(PaymentTransactionModel payment) {
        PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        return paymentMethodData != null && paymentMethodData.getId().equals(payment.getPaymentMethodId());
    }

    private InvoiceRequest populateCashAmount(OrderModel order, Long customerId,
                                              Double finalPrice, Optional<PaymentTransactionModel> cashPaymentTransaction) {
        if (finalPrice > 0) {
            if (!cashPaymentTransaction.isPresent()) {
                LOGGER.warn("Order has not cash payment: {}", order.getCode());
                return null;
            }
            PaymentTransactionModel cashTransaction = cashPaymentTransaction.get();
            double amount = OrderType.ONLINE.toString().equals(order.getType()) ||
                    OrderType.WHOLESALE.toString().equals(order.getType()) ? cashTransaction.getAmount() : finalPrice;
            return populateInvoiceRequest(order, customerId, cashTransaction, amount);
        }

        if (cashPaymentTransaction.isPresent() && finalPrice <= 0) {
            PaymentTransactionModel cashTransaction = cashPaymentTransaction.get();
            //Change cash invoice to 0.
            return populateInvoiceRequest(order, customerId, cashTransaction, 0d);
        }

        return null;
    }

    private InvoiceRequest populateInvoiceRequest(OrderModel order, Long customerId,
                                                  PaymentTransactionModel payment, Double amount) {
        InvoiceRequest request = new InvoiceRequest();
        request.setPaymentTransactionId(payment.getId());
        request.setDeleted(payment.isDeleted());
        request.setAmount(amount);
        request.setCompanyId(order.getCompanyId());
        request.setEmployeeId(order.getCreateByUser());
        request.setMoneySourceId(payment.getMoneySourceId());
        request.setOrderCodes(order.getCode());
        request.setOrderType(order.getType());
        request.setPaymentMethodId(payment.getPaymentMethodId());
        populateTransactionDate(order, payment, request);
        request.setWarehouseId(payment.getWarehouseId());
        request.setObjectId(customerId);
        return request;
    }

    private void populateTransactionDate(OrderModel order, PaymentTransactionModel payment, InvoiceRequest request) {
        if (!OrderType.ONLINE.toString().equals(order.getType())) {
            request.setTransactionDate(order.getCreatedTime());
            return;
        }

        if(isRedeemLoyaltyPayment(payment)) {
            Date transactionDate = orderHistoryService.getLastCompletedDateOf(order);
            transactionDate = transactionDate == null ? Calendar.getInstance().getTime() : transactionDate;
            request.setTransactionDate(transactionDate);
            return;
        }

        request.setTransactionDate(payment.getCreatedTime());
    }

    protected void saveInvoiceToPayments(Collection<PaymentTransactionModel> paymentTransactions, InvoiceOrderData invoiceOrderData) {
        Map<Long, Long> invoicePaymentMap = invoiceOrderData.getInvoicePaymentMap();
        if (MapUtils.isNotEmpty(invoicePaymentMap)) {
            List<PaymentTransactionModel> updatedPayments = paymentTransactions.stream().collect(Collectors.toList());
            for (PaymentTransactionModel payment : updatedPayments) {
                Long invoiceId = invoicePaymentMap.get(payment.getId());
                payment.setInvoiceId(invoiceId);
            }
            paymentTransactionService.saveAll(updatedPayments);
        }
    }

    @Override
    @Transactional
    public void createInvoiceForReturnOrder(ReturnOrderEvent event) {
        ReturnOrderModel returnOrder = event.getReturnOrder();
        OrderModel originOrder = returnOrder.getOriginOrder();
        BillRequest billRequest = event.getBillRequest();
        double receiptFinalPrice = billRequest.getFinalCost() == null ? 0 : billRequest.getFinalCost();
        OrderModel exchangeOrder = returnOrder.getExchangeOrder();
        double finalExchangeOrderPrice = calculateFinalExchange(exchangeOrder);

        double refundAmount = CommonUtils.readValue(returnOrder.getRefundAmount());
        double compensateRevert = CommonUtils.readValue(returnOrder.getCompensateRevert());
        double vatAmount = CommonUtils.readValue(returnOrder.getVat());
        double shippingAmount = CommonUtils.readValue(returnOrder.getShippingFee());

        double returnOrderMoney = finalExchangeOrderPrice + refundAmount + compensateRevert - (receiptFinalPrice + vatAmount + shippingAmount);

        if (returnOrderMoney == 0 && refundAmount == 0) {
            LOGGER.warn("Return order ({}) with zero price, not create invoices.", returnOrder.getId());
            return;
        }

        InvoiceType invoiceType = returnOrderMoney < 0 ? InvoiceType.IMBURSEMENT : InvoiceType.RECEIPT;
        List<PaymentTransactionModel> paymentTransactions = paymentTransactionService.findAllByReturnOrder(returnOrder);
        if (CollectionUtils.isEmpty(paymentTransactions)) {
            LOGGER.warn("Empty payment transactions of return: {}", returnOrder.getId());
            return;
        }
        List<InvoiceRequest> invoiceOthers = new ArrayList<>();
        List<InvoiceRequest> invoiceRefund = new ArrayList<>();
        PaymentMethodData loyaltyPaymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        Set<PaymentTransactionModel> otherPayment = paymentTransactions.stream()
                .filter(p -> !loyaltyPaymentMethodData.getId().equals(p.getPaymentMethodId()))
                .collect(Collectors.toSet());

        Optional<PaymentTransactionModel> refundPaymentOption = paymentTransactions.stream().
                filter(p -> loyaltyPaymentMethodData.getId().equals(p.getPaymentMethodId())).findFirst();

        if (CollectionUtils.isNotEmpty(otherPayment)) {
            for (PaymentTransactionModel payment : otherPayment) {
                InvoiceRequest request = populateInvoiceRequestForReturn(returnOrder, originOrder, payment);
                invoiceOthers.add(request);
                InvoiceOrderData invoiceReturnOther = financeService.createInvoiceReturnOrder(invoiceOthers, invoiceType.toString());
                saveInvoiceToPayments(otherPayment, invoiceReturnOther);
            }
        }

        if (refundPaymentOption.isPresent()) {
            PaymentTransactionModel refundPayment = refundPaymentOption.get();
            refundPayment.setConversionRate(returnOrder.getConversionRate());
            InvoiceRequest request = populateInvoiceRequestForReturn(returnOrder, originOrder, refundPayment);
            invoiceRefund.add(request);
            InvoiceOrderData invoiceReturnRefund = financeService.createInvoiceReturnOrder(invoiceRefund, InvoiceType.IMBURSEMENT.name());
            saveInvoiceToPayments(Arrays.asList(refundPayment), invoiceReturnRefund);
        }
        LOGGER.debug("Create invoice success for return order: {}", returnOrder.getId());
    }

    private double calculateFinalExchange(OrderModel exchangeOrder) {
        double finalExchangeOrderPrice = 0;
        if (exchangeOrder != null) {
            finalExchangeOrderPrice = CommonUtils.readValue(exchangeOrder.getFinalPrice()) - CommonUtils.readValue(exchangeOrder.getRedeemAmount());
        }
        return finalExchangeOrderPrice;
    }

    private InvoiceRequest populateInvoiceRequestForReturn(ReturnOrderModel returnOrder, OrderModel originOrder, PaymentTransactionModel payment) {
        InvoiceRequest request = new InvoiceRequest();
        request.setPaymentTransactionId(payment.getId());
        request.setAmount(payment.getAmount());
        request.setCompanyId(originOrder.getCompanyId());
        request.setEmployeeId(returnOrder.getCreatedBy());
        request.setMoneySourceId(payment.getMoneySourceId());
        request.setPaymentMethodId(payment.getPaymentMethodId());
        request.setDeleted(payment.isDeleted());
        Date transactionDate = payment.getTransactionDate() == null ? payment.getCreatedTime() : payment.getTransactionDate();
        request.setTransactionDate(transactionDate);
        request.setWarehouseId(payment.getWarehouseId());
        request.setObjectId(originOrder.getCustomerId());
        request.setReferId(returnOrder.getId().toString());
        request.setReferType(ReferType.RETURN_ORDER.code());
        request.setOrderType(originOrder.getType());
        return request;
    }

    @Override
    public void unverifyInvoices(OrderModel order) {
        Set<PaymentTransactionModel> paymentTransactions = order.getPaymentTransactions();
        if (CollectionUtils.isEmpty(paymentTransactions)) {
            return;
        }
        List<Long> invoiceIds = paymentTransactions.stream().map(paymentTransactionModel -> paymentTransactionModel.getInvoiceId())
                .collect(Collectors.toList());
        financeService.unverifyInvoiceOrder(invoiceIds, order.getCode());
    }

    @Override
    public void updateRefundInvoice(ReturnOrderModel returnOrderModel) {
        Double refundAmount = returnOrderModel.getRefundAmount();
        Set<PaymentTransactionModel> paymentTransactions = returnOrderModel.getPaymentTransactions();
        PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        Optional<PaymentTransactionModel> refundPaymentOption = paymentTransactions.stream().
                filter(p -> paymentMethodData.getId().equals(p.getPaymentMethodId())).findFirst();
        if (refundPaymentOption.isPresent()) {
            PaymentTransactionModel refundPayment = refundPaymentOption.get();
            refundPayment.setAmount(refundAmount);
            refundPayment.setConversionRate(returnOrderModel.getConversionRate());
            InvoiceRequest request = populateInvoiceRequestForReturn(returnOrderModel, returnOrderModel.getOriginOrder(), refundPayment);
            InvoiceOrderData invoiceReturnRefund = financeService.createInvoiceReturnOrder(Arrays.asList(request), InvoiceType.IMBURSEMENT.name());
            saveInvoiceToPayments(Arrays.asList(refundPayment), invoiceReturnRefund);
        }

    }

    @Override
    public void reverseVerifyInvoiceWithOnline(OrderModel order) {
        financeService.reverseVerifyInvoiceWithOnline(order.getCode(), order.getCompanyId());
    }

    @Override
    public List<InvoiceData> findAllOrderInvoices(Long companyId, String orderCode, Long returnOrderId, String orderType) {
        return financeService.findAllOrderInvoices(companyId, orderCode, returnOrderId, orderType);
    }

    @Override
    @Transactional
    public void saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(OrderModel order) {
        if(!OrderType.ONLINE.toString().equals(order.getType())
                && !OrderStatus.CHANGE_TO_RETAIL.code().equals(order.getOrderStatus())) {
            LOGGER.warn("Order type is not online and not retail with change to retail status: code: {}", order.getCode());
            return;
        }

        if(OrderType.ONLINE.toString().equals(order.getType()) && OrderStatus.CHANGE_TO_RETAIL.code().equals(order.getOrderStatus())) {
            LOGGER.warn("Not accept online with change to retail status: code: {}", order.getCode());
            return;
        }

        saveInvoices(order, order.getCustomerId());
    }

    @Override
    public void cancelLoyaltyRedeemInvoice(OrderModel order) {
        PaymentTransactionModel loyaltyRedeem = paymentTransactionService.findLoyaltyRedeem(order);
        if(loyaltyRedeem == null) {
            return;
        }

        Long invoiceId = loyaltyRedeem.getInvoiceId();
        if(invoiceId == null) {
            return;
        }
        LoyaltyInvoiceData loyaltyInvoiceData = new LoyaltyInvoiceData();
        loyaltyInvoiceData.setCompanyId(order.getCompanyId());
        loyaltyInvoiceData.setWarehouseId(order.getWarehouseId());
        loyaltyInvoiceData.setReferId(order.getCode());
        loyaltyInvoiceData.setReferType(order.getType());
        loyaltyInvoiceData.setOrderType(order.getType());
        loyaltyInvoiceData.setInvoiceId(invoiceId);
        loyaltyRedeem.setDeleted(true);
        paymentTransactionService.save(loyaltyRedeem);
        loyaltyInvoiceProducerService.produceCancelLoyaltyRedeemInvoice(loyaltyInvoiceData);
    }

    @Override
    public void cancelLoyaltyRewardInvoice(OrderModel order) {
        LoyaltyInvoiceData loyaltyInvoiceData = new LoyaltyInvoiceData();
        loyaltyInvoiceData.setCompanyId(order.getCompanyId());
        loyaltyInvoiceData.setWarehouseId(order.getWarehouseId());
        loyaltyInvoiceData.setReferId(order.getCode());
        loyaltyInvoiceData.setReferType(order.getType());
        loyaltyInvoiceData.setOrderType(order.getType());
        loyaltyInvoiceProducerService.produceCancelLoyaltyRewardInvoice(loyaltyInvoiceData);
    }

    @Override
    @Transactional
    public void mapInvoiceToPaymentTransaction(PaymentInvoiceData paymentInvoiceData) {
        Long paymentTransactionId = paymentInvoiceData.getPaymentTransactionId();
        if(paymentTransactionId == null) {
            LOGGER.warn("Canot mapping invoice to payment with id is null");
            return;
        }

        PaymentTransactionModel model = paymentTransactionService.findById(paymentTransactionId);
        if(model == null) {
            LOGGER.warn("Not found payment for mapping invoice: paymentId: {}", paymentTransactionId);
            return;
        }
        model.setInvoiceId(paymentInvoiceData.getInvoiceId());
        paymentTransactionService.save(model);
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setFinanceService(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Autowired
    public void setLoyaltyInvoiceProducerService(LoyaltyInvoiceProducerService loyaltyInvoiceProducerService) {
        this.loyaltyInvoiceProducerService = loyaltyInvoiceProducerService;
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }
}
