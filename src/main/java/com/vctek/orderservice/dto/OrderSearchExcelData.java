package com.vctek.orderservice.dto;
import java.util.ArrayList;
import java.util.List;

public class OrderSearchExcelData {
    private List<OrderEntryExcelData> orderEntryExcelData = new ArrayList<>();
    private byte[] content;
    private List<PaymentMethodData> paymentMethod = new ArrayList<>();

    public List<OrderEntryExcelData> getOrderEntryExcelData() {
        return orderEntryExcelData;
    }

    public void setOrderEntryExcelData(List<OrderEntryExcelData> orderEntryExcelData) {
        this.orderEntryExcelData = orderEntryExcelData;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public List<PaymentMethodData> getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(List<PaymentMethodData> paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
