package com.vctek.orderservice.model;

import javax.persistence.*;

@Entity
@Table(name = "order_setting_discount")
public class OrderSettingDiscountModel extends AuditModel {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "deleted")
    private boolean deleted;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_setting_id", unique = true)
    private OrderSettingModel orderSetting;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public OrderSettingModel getSettingModel() {
        return orderSetting;
    }

    public void setSettingModel(OrderSettingModel orderSetting) {
        this.orderSetting = orderSetting;
    }
}
