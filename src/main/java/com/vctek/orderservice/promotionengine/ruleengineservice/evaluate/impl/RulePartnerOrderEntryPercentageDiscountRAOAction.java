package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.OrderEntrySelectionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component("rulePartnerOrderEntryPercentageDiscountRAOAction")
public class RulePartnerOrderEntryPercentageDiscountRAOAction extends AbstractRulePartnerProductAction {

    public static final String QUALIFYING_CONTAINERS_PARAM = "qualifying_containers";
    public static final String PARTNER_CONTAINERS_PARAM = "target_containers";

    public boolean performActionInternal(RuleActionContext context) {
        OrderEntrySelectionStrategy selectionStrategy = (OrderEntrySelectionStrategy)context.getParameter("selection_strategy");
        Map<String, Integer> qualifyingProductsContainers = (Map)context.getParameter(QUALIFYING_CONTAINERS_PARAM);
        Map<String, Integer> partnerProductsContainers = (Map)context.getParameter(PARTNER_CONTAINERS_PARAM);
        if (Objects.isNull(selectionStrategy)) {
            selectionStrategy = OrderEntrySelectionStrategy.DEFAULT;
        }

        List<EntriesSelectionStrategyRPD> selectionStrategyRPDs = Lists.newArrayList();
        List<EntriesSelectionStrategyRPD> triggeringSelectionStrategyRPDs = this.createSelectionStrategyRPDsQualifyingProducts(context, selectionStrategy, qualifyingProductsContainers);
        selectionStrategyRPDs.addAll(triggeringSelectionStrategyRPDs);
        List<EntriesSelectionStrategyRPD> targetingSelectionStrategyRPDs = this.createSelectionStrategyRPDsTargetProducts(context, selectionStrategy, partnerProductsContainers);
        selectionStrategyRPDs.addAll(targetingSelectionStrategyRPDs);
        return this.extractAmountForCurrency(context, context.getParameter("value")).map((amount) -> this.performAction(context, selectionStrategyRPDs, amount)).orElse(false);
    }

    protected boolean performAction(RuleActionContext context, List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs, BigDecimal amount) {
        boolean isPerformed = false;
        this.validateSelectionStrategy(entriesSelectionStrategyRPDs, context);
        if (this.hasEnoughQuantity(context, entriesSelectionStrategyRPDs)) {
            boolean isTheSameTargetAndTriggerEntry = this.adjustStrategyQuantityAndGetTheSameTargetAndTriggerEntry(entriesSelectionStrategyRPDs, -1);
            isPerformed = true;
            List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForAction = new ArrayList<>();
            List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForTriggering = new ArrayList<>();
            this.splitEntriesSelectionStrategies(entriesSelectionStrategyRPDs, selectionStrategyRPDsForAction, selectionStrategyRPDsForTriggering);
            List<DiscountRAO> discounts = this.addDiscountAndConsume(context, selectionStrategyRPDsForAction, false, amount, isTheSameTargetAndTriggerEntry);
            if (CollectionUtils.isNotEmpty(selectionStrategyRPDsForTriggering)) {
                this.consumeOrderEntries(selectionStrategyRPDsForTriggering, discounts.isEmpty() ? null : discounts.get(0));
                this.updateFactsWithOrderEntries(context, selectionStrategyRPDsForTriggering);
            }
        }

        return isPerformed;
    }

    protected void updateFactsWithOrderEntries(RuleActionContext context, List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDS) {
        Iterator var4 = entriesSelectionStrategyRPDS.iterator();

        while(var4.hasNext()) {
            EntriesSelectionStrategyRPD selectionStrategyRPDForTriggering = (EntriesSelectionStrategyRPD)var4.next();
            Iterator var6 = selectionStrategyRPDForTriggering.getOrderEntries().iterator();

            while(var6.hasNext()) {
                OrderEntryRAO orderEntryRao = (OrderEntryRAO)var6.next();
                context.scheduleForUpdate(new Object[]{orderEntryRao});
            }
        }

    }

    protected List<DiscountRAO> addDiscountAndConsume(RuleActionContext context, List<EntriesSelectionStrategyRPD> selectionStrategies, boolean absolute, BigDecimal price, boolean isTheSameTargetAndTriggerEntry) {
        Map<Integer, Integer> selectedOrderEntryMap = this.getSelectedOrderEntryQuantities(selectionStrategies);
        Set<OrderEntryRAO> selectedOrderEntryRaos = this.getSelectedOrderEntryRaos(selectionStrategies, selectedOrderEntryMap);
        List<DiscountRAO> discounts = this.getRuleEngineCalculationService().addOrderEntryLevelDiscount(selectedOrderEntryMap, selectedOrderEntryRaos, absolute, price);

        DiscountRAO discount;
        for(Iterator var9 = discounts.iterator(); var9.hasNext(); context.scheduleForUpdate(new Object[]{discount.getAppliedToObject()})) {
            discount = (DiscountRAO)var9.next();
            discount.setTheSamePartnerProduct(isTheSameTargetAndTriggerEntry);
            OrderEntryRAO entry = (OrderEntryRAO)discount.getAppliedToObject();
            this.consumeOrderEntry(entry, selectedOrderEntryMap.get(entry.getEntryNumber()), this.adjustUnitPrice(entry), discount);
            if (!this.mergeDiscounts(context, discount, entry)) {
                RuleEngineResultRAO result = context.getRuleEngineResultRao();
                result.getActions().add(discount);
                this.setRAOMetaData(context, new AbstractRuleActionRAO[]{discount});
                context.insertFacts(new Object[]{discount});
                context.insertFacts(discount.getConsumedEntries());
                final String firedRuleCode = discount.getFiredRuleCode();
                discount.getConsumedEntries().forEach((coe) -> coe.setFiredRuleCode(firedRuleCode));
                context.scheduleForUpdate(new Object[]{result});
            }
        }

        if (CollectionUtils.isNotEmpty(selectedOrderEntryRaos)) {
            CartRAO cartRAO = (CartRAO) selectedOrderEntryRaos.iterator().next().getOrder();
            context.scheduleForUpdate(new Object[]{cartRAO});
        }

        return discounts;
    }
}
