package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class OrderModel extends AbstractOrderModel {

    @Column(name = "bill_id")
    private Long billId;

    @OneToMany(mappedBy = "originOrder", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Set<ReturnOrderModel> returnOrders = new HashSet<>();

    @OneToOne(mappedBy = "exchangeOrder", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private ReturnOrderModel returnOrder;

    @Column(name = "shipping_company_id")
    private Long shippingCompanyId;

    @Column(name = "shipping_fee_setting_id")
    private Long shippingFeeSettingId;

    @Column(name = "order_retail_code")
    private String orderRetailCode;

    @Column(name = "age")
    private String age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "redeem_amount")
    private Double redeemAmount;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "confirm_discount_by")
    private Long confirmDiscountBy;

    @Column(name = "import_order_processing")
    private boolean importOrderProcessing;

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }


    public ReturnOrderModel getReturnOrder() {
        return returnOrder;
    }

    public void setReturnOrder(ReturnOrderModel returnOrder) {
        this.returnOrder = returnOrder;
    }

    public Set<ReturnOrderModel> getReturnOrders() {
        return returnOrders;
    }

    public void setReturnOrders(Set<ReturnOrderModel> returnOrders) {
        this.returnOrders = returnOrders;
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

    public Double getRedeemAmount() {
        return redeemAmount;
    }

    public void setRedeemAmount(Double redeemAmount) {
        this.redeemAmount = redeemAmount;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Long getConfirmDiscountBy() {
        return confirmDiscountBy;
    }

    public void setConfirmDiscountBy(Long confirmDiscountBy) {
        this.confirmDiscountBy = confirmDiscountBy;
    }

    public boolean isImportOrderProcessing() {
        return importOrderProcessing;
    }

    public void setImportOrderProcessing(boolean importOrderProcessing) {
        this.importOrderProcessing = importOrderProcessing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderModel)) return false;
        OrderModel that = (OrderModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Long getShippingFeeSettingId() {
        return shippingFeeSettingId;
    }

    public void setShippingFeeSettingId(Long shippingFeeSettingId) {
        this.shippingFeeSettingId = shippingFeeSettingId;
    }
}
