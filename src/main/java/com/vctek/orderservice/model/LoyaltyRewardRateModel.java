package com.vctek.orderservice.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "loyalty_reward_rate")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@EntityListeners({AuditingEntityListener.class})
public class LoyaltyRewardRateModel extends ItemModel {
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "reward_rate")
    private Double rewardRate;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Double getRewardRate() {
        return rewardRate;
    }

    public void setRewardRate(Double rewardRate) {
        this.rewardRate = rewardRate;
    }
}