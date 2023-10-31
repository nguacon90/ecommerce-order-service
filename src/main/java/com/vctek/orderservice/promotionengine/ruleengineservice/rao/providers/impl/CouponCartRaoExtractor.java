package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOFactsExtractor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component("couponCartRaoExtractor")
public class CouponCartRaoExtractor implements RAOFactsExtractor {
    public static final String EXPAND_COUPONS = "EXPAND_COUPONS";
    @Override
    public Set expandFact(Object fact) {
        Preconditions.checkArgument(fact instanceof CartRAO, "CartRAO type is expected here");
        Set<Object> facts = new HashSet();
        CartRAO cartRAO = (CartRAO)fact;
        if (CollectionUtils.isNotEmpty(cartRAO.getCoupons())) {
            facts.addAll(cartRAO.getCoupons());
        }

        return facts;
    }

    @Override
    public String getTriggeringOption() {
        return EXPAND_COUPONS;
    }

    @Override
    public boolean isMinOption() {
        return false;
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
