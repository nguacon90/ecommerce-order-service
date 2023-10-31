package com.vctek.orderservice.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "loyalty_redeem_rate")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@EntityListeners({AuditingEntityListener.class})
public class LoyaltyRedeemRateUseModel extends ItemModel {
    @Column(name = "company_id")
    private Long companyId;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
