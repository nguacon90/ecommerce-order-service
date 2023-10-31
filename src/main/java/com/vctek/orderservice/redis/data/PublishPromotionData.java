package com.vctek.orderservice.redis.data;

import java.io.Serializable;

public class PublishPromotionData implements Serializable {
    private Long droolsRuleId;
    private Long promotionSourceRuleId;
    private Long kieModuleId;

    public Long getDroolsRuleId() {
        return droolsRuleId;
    }

    public void setDroolsRuleId(Long droolsRuleId) {
        this.droolsRuleId = droolsRuleId;
    }

    public Long getPromotionSourceRuleId() {
        return promotionSourceRuleId;
    }

    public void setPromotionSourceRuleId(Long promotionSourceRuleId) {
        this.promotionSourceRuleId = promotionSourceRuleId;
    }

    public Long getKieModuleId() {
        return kieModuleId;
    }

    public void setKieModuleId(Long kieModuleId) {
        this.kieModuleId = kieModuleId;
    }
}
