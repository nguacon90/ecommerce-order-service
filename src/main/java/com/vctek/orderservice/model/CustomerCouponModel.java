package com.vctek.orderservice.model;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "customer_coupon")
public class CustomerCouponModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "user_id")
    private Long userId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "coupon_code_id")
    private CouponCodeModel couponCodeModel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CouponCodeModel getCouponCodeModel() {
        return couponCodeModel;
    }

    public void setCouponCodeModel(CouponCodeModel couponCodeModel) {
        this.couponCodeModel = couponCodeModel;
    }
}
