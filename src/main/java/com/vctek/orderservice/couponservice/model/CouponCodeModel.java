package com.vctek.orderservice.couponservice.model;

import com.vctek.orderservice.model.CustomerCouponModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.model.OrderHasCouponCodeModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coupon_code")
public class CouponCodeModel extends ItemModel {

    @Column(name = "code")
    private String code;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "coupon_id")
    private CouponModel coupon;

    @OneToMany(mappedBy = "couponCodeModel", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<CouponRedemptionModel> couponRedemptionModels = new HashSet<>();

    @OneToMany(mappedBy = "couponCode", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = new HashSet<>();

    @OneToOne(mappedBy = "couponCodeModel", cascade = {CascadeType.PERSIST})
    private CustomerCouponModel customerCouponModel;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CouponModel getCoupon() {
        return coupon;
    }

    public void setCoupon(CouponModel coupon) {
        this.coupon = coupon;
    }

    public Set<CouponRedemptionModel> getCouponRedemptionModels() {
        return couponRedemptionModels;
    }

    public void setCouponRedemptionModels(Set<CouponRedemptionModel> couponRedemptionModels) {
        this.couponRedemptionModels = couponRedemptionModels;
    }

    public Set<OrderHasCouponCodeModel> getOrderHasCouponCodeModels() {
        return orderHasCouponCodeModels;
    }

    public void setOrderHasCouponCodeModels(Set<OrderHasCouponCodeModel> orderHasCouponCodeModels) {
        this.orderHasCouponCodeModels = orderHasCouponCodeModels;
    }

    public CustomerCouponModel getCustomerCouponModel() {
        return customerCouponModel;
    }

    public void setCustomerCouponModel(CustomerCouponModel customerCouponModel) {
        this.customerCouponModel = customerCouponModel;
    }
}
