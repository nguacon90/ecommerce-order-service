package com.vctek.orderservice.dto;

import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.PaymentTransactionModel;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommerceCheckoutParameter {
    private CartModel cart;
    private Set<PaymentTransactionModel> paymentTransactions;
    private String note;
    private String vatNumber;
    private Date vatDate;
    private CustomerRequest customerRequest;
    private Date deliveryDate;
    private String customerNote;
    private String customerSupportNote;
    private Long shippingCompanyId;
    private Long shippingFeeSettingId;
    private String orderRetailCode;
    private Long orderSourceId;
    private Long employeeId;
    private Double deliveryCost;
    private Double collaboratorShippingFee;
    private Double companyShippingFee;
    private String age;
    private String gender;
    private String cardNumber;
    private Long confirmDiscountBy;
    private List<Long> settingCustomerOptionIds;
    private Long createdByUser;
    private Map<Long, Double> productWeight;


    public CartModel getCart() {
        return cart;
    }

    public void setCart(CartModel cart) {
        this.cart = cart;
    }

    public Set<PaymentTransactionModel> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(Set<PaymentTransactionModel> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public Date getVatDate() {
        return vatDate;
    }

    public void setVatDate(Date vatDate) {
        this.vatDate = vatDate;
    }

    public CustomerRequest getCustomerRequest() {
        return customerRequest;
    }

    public void setCustomerRequest(CustomerRequest customerRequest) {
        this.customerRequest = customerRequest;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public String getCustomerSupportNote() {
        return customerSupportNote;
    }

    public void setCustomerSupportNote(String customerSupportNote) {
        this.customerSupportNote = customerSupportNote;
    }

    public Long getShippingCompanyId() {
        return shippingCompanyId;
    }

    public void setShippingCompanyId(Long shippingCompanyId) {
        this.shippingCompanyId = shippingCompanyId;
    }

    public String getOrderRetailCode() {
        return orderRetailCode;
    }

    public void setOrderRetailCode(String orderRetailCode) {
        this.orderRetailCode = orderRetailCode;
    }

    public Long getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSourceId(Long orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Double getCollaboratorShippingFee() {
        return collaboratorShippingFee;
    }

    public void setCollaboratorShippingFee(Double collaboratorShippingFee) {
        this.collaboratorShippingFee = collaboratorShippingFee;
    }

    public Double getCompanyShippingFee() {
        return companyShippingFee;
    }

    public void setCompanyShippingFee(Double companyShippingFee) {
        this.companyShippingFee = companyShippingFee;
    }

    public Long getConfirmDiscountBy() {
        return confirmDiscountBy;
    }

    public void setConfirmDiscountBy(Long confirmDiscountBy) {
        this.confirmDiscountBy = confirmDiscountBy;
    }

    public List<Long> getSettingCustomerOptionIds() {
        return settingCustomerOptionIds;
    }

    public void setSettingCustomerOptionIds(List<Long> settingCustomerOptionIds) {
        this.settingCustomerOptionIds = settingCustomerOptionIds;
    }

    public Long getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(Long createdByUser) {
        this.createdByUser = createdByUser;
    }

    public Map<Long, Double> getProductWeight() {
        return productWeight;
    }

    public void setProductWeight(Map<Long, Double> productWeight) {
        this.productWeight = productWeight;
    }

    public Long getShippingFeeSettingId() {
        return shippingFeeSettingId;
    }

    public void setShippingFeeSettingId(Long shippingFeeSettingId) {
        this.shippingFeeSettingId = shippingFeeSettingId;
    }
}
