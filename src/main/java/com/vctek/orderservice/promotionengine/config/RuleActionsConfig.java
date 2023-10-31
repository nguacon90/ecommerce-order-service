package com.vctek.orderservice.promotionengine.config;

import com.vctek.orderservice.promotionengine.ruledefinition.actions.DefaultRuleExecutableAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleActionsConfig {

    @Bean("ruleOrderFixedDiscountAction")
    public DefaultRuleExecutableAction ruleOrderFixedDiscountAction(
            @Qualifier("ruleOrderFixedDiscountRAOAction") RuleOrderFixedDiscountRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

    @Bean("ruleOrderPercentageDiscountAction")
    public DefaultRuleExecutableAction ruleOrderPercentageDiscountAction(
            @Qualifier("ruleOrderPercentageDiscountRAOAction") RuleOrderPercentageDiscountRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

    @Bean("ruleEmployeeOrderPercentageDiscountAction")
    public DefaultRuleExecutableAction ruleEmployeeOrderPercentageDiscountAction(
            @Qualifier("ruleEmployeeOrderPercentageDiscountRAOAction") RuleEmployeeOrderPercentageDiscountRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

    @Bean("ruleOrderEntryFixedDiscountAction")
    public DefaultRuleExecutableAction ruleOrderEntryFixedDiscountAction(
            @Qualifier("ruleOrderEntryFixedDiscountRAOAction") RuleOrderEntryFixedDiscountRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

    @Bean("ruleOrderEntryPercentageDiscountAction")
    public DefaultRuleExecutableAction ruleOrderEntryPercentageDiscountAction(
            @Qualifier("ruleOrderEntryPercentageDiscountRAOAction") RuleOrderEntryPercentageDiscountRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

    @Bean("ruleFreeGiftDiscountAction")
    public DefaultRuleExecutableAction ruleFreeGiftDiscountAction(
            @Qualifier("ruleFreeGiftRAOAction") RuleFreeGiftRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

    @Bean("ruleOrderEntryFixedPriceAction")
    public DefaultRuleExecutableAction ruleOrderEntryFixedPriceAction(
            @Qualifier("ruleOrderEntryFixedPriceRAOAction") RuleOrderEntryFixedPriceRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

    @Bean("rulePartnerOrderEntryPercentageDiscountAction")
    public DefaultRuleExecutableAction rulePartnerOrderEntryPercentageDiscountRAOAction(
            @Qualifier("rulePartnerOrderEntryPercentageDiscountRAOAction") RulePartnerOrderEntryPercentageDiscountRAOAction action) {
        DefaultRuleExecutableAction ruleExecutableAction = new DefaultRuleExecutableAction();
        ruleExecutableAction.setRaoAction(action);
        return ruleExecutableAction;
    }

}
