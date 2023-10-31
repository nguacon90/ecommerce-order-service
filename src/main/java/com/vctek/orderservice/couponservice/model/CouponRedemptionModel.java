package com.vctek.orderservice.couponservice.model;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;

@Entity
@Table(name = "coupon_redemption")
public class CouponRedemptionModel extends ItemModel {

    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "coupon_code_id")
    private CouponCodeModel couponCodeModel;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_id")
    private AbstractOrderModel order;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public CouponCodeModel getCouponCodeModel() {
        return couponCodeModel;
    }

    public void setCouponCodeModel(CouponCodeModel couponCodeModel) {
        this.couponCodeModel = couponCodeModel;
    }

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }
}
