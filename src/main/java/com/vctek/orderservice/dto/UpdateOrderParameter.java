package com.vctek.orderservice.dto;

import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.model.OrderModel;

import java.util.Date;

public class UpdateOrderParameter {
    private OrderModel order;
    private String note;
    private String vatNumber;
    private Date vatDate;
    private CustomerRequest customerRequest;
    private Long shippingCompanyId;
    private Double deliveryCost;
    private Double companyShippingFee;
    private Double collaboratorShippingFee;
    private String customerNote;
    private String customerSupportNote;
    private String age;
    private String gender;
    private String cardNumber;
    private Long orderSourceId;
    private Date deliveryDate;

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

    public OrderModel getOrder() {
        return order;
    }

    public void setOrder(OrderModel order) {
        this.order = order;
    }

    public Long getShippingCompanyId() {
        return shippingCompanyId;
    }

    public void setShippingCompanyId(Long shippingCompanyId) {
        this.shippingCompanyId = shippingCompanyId;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
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

    public Long getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSourceId(Long orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

    public Double getCompanyShippingFee() {
        return companyShippingFee;
    }

    public void setCompanyShippingFee(Double companyShippingFee) {
        this.companyShippingFee = companyShippingFee;
    }

    public Double getCollaboratorShippingFee() {
        return collaboratorShippingFee;
    }

    public void setCollaboratorShippingFee(Double collaboratorShippingFee) {
        this.collaboratorShippingFee = collaboratorShippingFee;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
