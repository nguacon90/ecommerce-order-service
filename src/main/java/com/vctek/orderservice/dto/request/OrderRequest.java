package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {
    private Long companyId;
    private String code;
    private String orderType;
    private String priceType;
    private CustomerRequest customer;
    private VatRequest vatInfo;
    private List<PaymentTransactionRequest> payments;
    private String note;
    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date deliveryDate;
    private String customerNote;
    private String customerSupportNote;

    private Long shippingCompanyId;
    private Long employeeId;
    private String orderRetailCode;
    private Long orderSourceId;
    private Double deliveryCost;
    private Double collaboratorShippingFee;
    private Double companyShippingFee;
    private String cardNumber;
    private boolean confirmDiscount;
    private String gender;
    private String age;
    private Long distributorId;
    private List<Long> settingCustomerOptionIds = new ArrayList<>();


    public VatRequest getVatInfo() {
        return vatInfo;
    }

    public void setVatInfo(VatRequest vatInfo) {
        this.vatInfo = vatInfo;
    }

    public List<PaymentTransactionRequest> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentTransactionRequest> payments) {
        this.payments = payments;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public CustomerRequest getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerRequest customer) {
        this.customer = customer;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
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

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public boolean isConfirmDiscount() {
        return confirmDiscount;
    }

    public void setConfirmDiscount(boolean confirmDiscount) {
        this.confirmDiscount = confirmDiscount;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public List<Long> getSettingCustomerOptionIds() {
        return settingCustomerOptionIds;
    }

    public void setSettingCustomerOptionIds(List<Long> settingCustomerOptionIds) {
        this.settingCustomerOptionIds = settingCustomerOptionIds;
    }
}
