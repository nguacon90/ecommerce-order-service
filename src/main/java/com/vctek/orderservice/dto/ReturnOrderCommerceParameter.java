package com.vctek.orderservice.dto;

import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;

import java.util.Set;


public class ReturnOrderCommerceParameter {
    private Long companyId;
    private OrderModel originOrder;
    private CartModel exchangeCart;
    private BillRequest billRequest;
    private String note;
    private Set<PaymentTransactionModel> paymentTransactions;
    private ReturnOrderRequest returnOrderRequest;
    private Set<PaymentTransactionModel> exchangePaymentTransactions;


    public OrderModel getOriginOrder() {
        return originOrder;
    }

    public void setOriginOrder(OrderModel originOrder) {
        this.originOrder = originOrder;
    }

    public CartModel getExchangeCart() {
        return exchangeCart;
    }

    public void setExchangeCart(CartModel exchangeCart) {
        this.exchangeCart = exchangeCart;
    }

    public BillRequest getBillRequest() {
        return billRequest;
    }

    public void setBillRequest(BillRequest billRequest) {
        this.billRequest = billRequest;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Set<PaymentTransactionModel> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(Set<PaymentTransactionModel> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public ReturnOrderRequest getReturnOrderRequest() {
        return returnOrderRequest;
    }

    public void setReturnOrderRequest(ReturnOrderRequest returnOrderRequest) {
        this.returnOrderRequest = returnOrderRequest;
    }

    public Set<PaymentTransactionModel> getExchangePaymentTransactions() {
        return exchangePaymentTransactions;
    }

    public void setExchangePaymentTransactions(Set<PaymentTransactionModel> exchangePaymentTransactions) {
        this.exchangePaymentTransactions = exchangePaymentTransactions;
    }
}
