package com.vctek.orderservice.model;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;

import javax.persistence.*;

@Entity
@Table(name = "orders_has_coupon_code")
public class OrderHasCouponCodeModel extends ItemModel {

    @Column(name = "redemption_quantity")
    private Integer redemptionQuantity;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_id")
    private AbstractOrderModel order;


    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "coupon_code_id")
    private CouponCodeModel couponCode;


    public Integer getRedemptionQuantity() {
        return redemptionQuantity;
    }

    public void setRedemptionQuantity(Integer redemptionQuantity) {
        this.redemptionQuantity = redemptionQuantity;
    }

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }

    public CouponCodeModel getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(CouponCodeModel couponCode) {
        this.couponCode = couponCode;
    }
}
