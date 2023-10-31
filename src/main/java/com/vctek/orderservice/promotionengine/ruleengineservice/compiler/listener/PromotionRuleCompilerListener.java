package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.listener;

import com.vctek.orderservice.promotionengine.promotionservice.strategy.ConditionResolutionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerListener;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderTypeRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.WarehouseRAO;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class PromotionRuleCompilerListener implements RuleCompilerListener {
    private Map<String, ConditionResolutionStrategy> conditionResolutionStrategies;

    public PromotionRuleCompilerListener(Map<String, ConditionResolutionStrategy> conditionResolutionStrategies) {
        this.conditionResolutionStrategies = conditionResolutionStrategies;
    }

    @Override
    public void beforeCompile(RuleCompilerContext context) {
        context.generateVariable(CartRAO.class);
        context.generateVariable(RuleEngineResultRAO.class);
        context.generateVariable(WarehouseRAO.class);
        context.generateVariable(OrderTypeRAO.class);
        this.cleanStoredParameterValues(context);
    }

    private void cleanStoredParameterValues(RuleCompilerContext context) {
        if (MapUtils.isNotEmpty(this.conditionResolutionStrategies)) {
            Iterator var3 = this.conditionResolutionStrategies.values().iterator();

            while(var3.hasNext()) {
                ConditionResolutionStrategy strategy = (ConditionResolutionStrategy)var3.next();
                strategy.cleanStoredParameterValues(context);
            }
        }
    }

    @Override
    public void afterCompile(RuleCompilerContext context) {
        //NOSONAR TODO implement
    }

    @Override
    public void afterCompileError(RuleCompilerContext context) {
        //NOSONAR TODO implement
    }
}
