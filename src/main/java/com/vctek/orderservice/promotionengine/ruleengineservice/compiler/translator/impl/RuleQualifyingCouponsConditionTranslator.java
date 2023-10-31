package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CouponRAO;
import com.vctek.orderservice.service.CouponService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("ruleQualifyingCouponsConditionTranslator")
public class RuleQualifyingCouponsConditionTranslator implements RuleConditionTranslator {
    public static final String COUPON_RAO_COUPON_ID_ATTRIBUTE = "couponId";
    public static final String COUPONS_PARAM = "coupons";
    private CouponService couponService;

    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition) {
        Preconditions.checkNotNull(context, "Rule Compiler Context is not expected to be NULL here");
        Preconditions.checkNotNull(condition, "Rule Condition Data is not expected to be NULL here");
        RuleParameterData couponsParameter = condition.getParameters().get(COUPONS_PARAM);
        if (couponsParameter == null) {
            return new RuleIrFalseCondition();
        }

        List<Long> coupons = (List) couponsParameter.getValue();
        PromotionSourceRuleModel rule = context.getRule();
        if (CollectionUtils.isEmpty(coupons)) {
            return new RuleIrFalseCondition();
        }

        RuleIrAttributeCondition irCouponCondition = new RuleIrAttributeCondition();
        String couponRaoVariable = context.generateVariable(CouponRAO.class);
        irCouponCondition.setVariable(couponRaoVariable);
        irCouponCondition.setAttribute(COUPON_RAO_COUPON_ID_ATTRIBUTE);
        irCouponCondition.setOperator(RuleIrAttributeOperator.IN);
        irCouponCondition.setValue(coupons);
        Map<String, Object> metadata = new HashMap();
        metadata.put("couponIds", coupons);
        irCouponCondition.setMetadata(metadata);
        couponService.updateUseForPromotion(coupons, rule);
        return irCouponCondition;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }
}
