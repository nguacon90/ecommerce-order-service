package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.processor;

import com.google.common.collect.Maps;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.RuleIrAttributeConditionBuilder;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.DefaultRuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrProcessor;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrExecutableAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrGroupCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrTypeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl.RuleCartTotalConditionTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CompanyRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PromotionRuleIrProcessor implements RuleIrProcessor {
    public static final String ACTION_PARAMETER_CART_THRESHOLD = "cart_threshold";
    public static final String CART_RAO_TOTAL_ATTRIBUTE = RuleCartTotalConditionTranslator.ORDER_RAO_TOTAL_ATTRIBUTE;
    public static final String CART_TOTAL_VALUE = RuleCartTotalConditionTranslator.VALUE_PARAM;
    public static final String CART_TOTAL_OPERATOR = "cart_total_operator";
    public static final String COMPANY_ATTR = "id";

    @Override
    public void process(DefaultRuleCompilerContext context, RuleIr ruleIr) {
        String orderRaoVariable = context.generateVariable(CartRAO.class);
        RuleIrTypeCondition irOrderCondition = new RuleIrTypeCondition();
        irOrderCondition.setVariable(orderRaoVariable);

        List<RuleIrCondition> conditions = ruleIr.getConditions();
        conditions.add(irOrderCondition);

        String resultRaoVariable = context.generateVariable(RuleEngineResultRAO.class);
        RuleIrTypeCondition irResultCondition = new RuleIrTypeCondition();
        irResultCondition.setVariable(resultRaoVariable);
        conditions.add(irResultCondition);

        PromotionSourceRuleModel rule = context.getRule();
        String companyRaoVariable = context.generateVariable(CompanyRAO.class);
        RuleIrAttributeCondition irCompanyCondition = RuleIrAttributeConditionBuilder
                .newAttributeConditionFor(companyRaoVariable)
                .withAttribute(COMPANY_ATTR)
                .withOperator(RuleIrAttributeOperator.EQUAL).withValue(rule.getCompanyId()).build();
        conditions.add(irCompanyCondition);

        Map<String, Object> sharedConditionParameters = this.getSharedConditionParameters(ruleIr.getConditions());
        if (!sharedConditionParameters.isEmpty()) {
            ruleIr.getActions().stream().filter((action) -> action instanceof RuleIrExecutableAction).forEach((action) -> {
                Map<String, Object> actionParameters = Maps.newHashMap(((RuleIrExecutableAction) action).getActionParameters());
                actionParameters.putAll(sharedConditionParameters);
                ((RuleIrExecutableAction) action).setActionParameters(actionParameters);
            });
        }
    }

    private Map<String, Object> getSharedConditionParameters(List<RuleIrCondition> conditions) {
        Map<String, Object> result = new HashMap();
        Collection<RuleIrGroupCondition> cartTotalConditions = this.getOrderTotalThresholdConditions(conditions);
        Map<String, Object> cartThresholdActionParam = this.getCartThresholdActionParam(cartTotalConditions);
        if (!cartThresholdActionParam.isEmpty()) {
            result.put(ACTION_PARAMETER_CART_THRESHOLD, cartThresholdActionParam);
        }

        return result;
    }

    private Map<String, Object> getCartThresholdActionParam(Collection<RuleIrGroupCondition> cartTotalConditions) {
        Map<String, Object> result = new HashMap();
        cartTotalConditions.stream().forEach((ruleIrGroupCondition) -> {

            Optional<RuleIrAttributeCondition> cartTotalAttributeCondition = ruleIrGroupCondition.getChildren().stream()
                    .filter((c) -> c instanceof RuleIrAttributeCondition)
                    .map(RuleIrAttributeCondition.class::cast)
                    .filter((c) -> CART_RAO_TOTAL_ATTRIBUTE.equals(c.getAttribute())).findAny();

            Object cartTotalValue = cartTotalAttributeCondition.map((c) -> c.getValue()).orElse(null);
            Object cartTotalOperator = cartTotalAttributeCondition.map((c) -> c.getOperator()).orElse(null);
            if (Objects.nonNull(cartTotalValue) && Objects.nonNull(cartTotalOperator)) {
                result.put(CART_TOTAL_VALUE, cartTotalValue);
                result.put(CART_TOTAL_OPERATOR, cartTotalOperator);
            }

        });
        return result;
    }

    protected Collection<RuleIrGroupCondition> getOrderTotalThresholdConditions(Collection<RuleIrCondition> conditions) {
        return conditions.stream().map((c) -> this.getOrderTotalThresholdConditions(c))
                .flatMap((c) -> c.stream()).collect(Collectors.toList());
    }

    protected Collection<RuleIrGroupCondition> getOrderTotalThresholdConditions(RuleIrCondition condition) {
        if (condition instanceof RuleIrGroupCondition) {
            RuleIrGroupCondition irGroupCondition = (RuleIrGroupCondition) condition;
            return this.isOrderTotalThresholdGroupCondition(irGroupCondition) ? Arrays.asList(irGroupCondition) :
                    irGroupCondition.getChildren().stream()
                            .map((children) -> this.getOrderTotalThresholdConditions(children))
                            .flatMap((c) -> c.stream()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    protected boolean isOrderTotalThresholdGroupCondition(RuleIrGroupCondition irGroupCondition) {
        return irGroupCondition.getChildren().stream()
                .filter((c) -> c instanceof RuleIrAttributeCondition)
                .map(RuleIrAttributeCondition.class::cast)
                .filter((c) -> CART_RAO_TOTAL_ATTRIBUTE.equals(c.getAttribute())).findAny().isPresent();
    }

}
