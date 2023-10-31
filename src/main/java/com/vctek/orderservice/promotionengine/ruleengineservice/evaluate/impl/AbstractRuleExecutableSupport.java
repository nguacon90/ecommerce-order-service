package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.RuleEngineCalculationService;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.EntriesSelectionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.OrderEntrySelectionStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.RAOAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractRuleExecutableSupport implements BeanNameAware, RAOAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRuleExecutableSupport.class);
    public static final String RULE_CODE = "ruleCode";
    public static final String MODULE_NAME = "moduleName";
    public static final String MAXIMUM_QUANTITY = "maximumQuantity";
    public static final String MAX_DISCOUNT_AMOUNT = "maxDiscountAmount";
    private String beanName;

    private boolean validateRuleCode;

    private boolean validateRulesModuleName;

    private RuleEngineCalculationService ruleEngineCalculationService;
    private Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> entriesSelectionStrategies;

    public void performAction(RuleActionContext context) {
        Preconditions.checkArgument(MapUtils.isNotEmpty(context.getParameters()),
                "Properties passed as a method argument must not be empty");
        this.validateRule(context);
        boolean allowedToExecute = this.allowedByRuntimeConfiguration(context);
        if (allowedToExecute && this.performActionInternal(context)) {
            this.trackConsumedProducts(context);
            this.trackRuleExecution(context);
            this.trackRuleGroupExecutions(context);
            context.updateScheduledFacts();
        }

    }

    protected boolean alwaysPerformAction(RuleActionContext context, OrderEntryRAO orderEntryRao,
                                          BigDecimal valueForCurrency, int consumableQuantity, int consumedQuantity, boolean isAbsolute) {
        DiscountRAO discount = this.getRuleEngineCalculationService().addOrderEntryLevelDiscountWithConsumableQty(orderEntryRao,
                isAbsolute, valueForCurrency, consumableQuantity);
        this.setRAOMetaData(context, new AbstractRuleActionRAO[]{discount});
        this.consumeOrderEntry(orderEntryRao, consumedQuantity, this.adjustUnitPrice(orderEntryRao, consumableQuantity), discount);
        RuleEngineResultRAO result = context.getRuleEngineResultRao();
        result.getActions().add(discount);
        context.scheduleForUpdate(new Object[]{orderEntryRao, orderEntryRao.getOrder(), result});
        context.insertFacts(new Object[]{discount});
        context.insertFacts(discount.getConsumedEntries());
        return true;
    }

    protected boolean performLimitedAction(RuleActionContext context, Set<OrderEntryRAO> orderEntries,
                                           Long maximumQuantity, BigDecimal amount, boolean isAbsolute) {
        List<OrderEntryRAO> sortedEntries = orderEntries.stream().collect(Collectors.toList());
        Collections.sort(sortedEntries, (o1, o2) -> o2.getBasePrice().compareTo(o1.getBasePrice()));
        boolean isPerformed = false;
        for (OrderEntryRAO orderEntryRAO : sortedEntries) {
            int consumableQuantity = this.getConsumableQuantity(orderEntryRAO);

            if (maximumQuantity == 0) {
                isPerformed |= this.alwaysPerformAction(context, orderEntryRAO, BigDecimal.ZERO, consumableQuantity, consumableQuantity, true);
                continue;
            }

            if (consumableQuantity <= maximumQuantity) {
                isPerformed |= this.alwaysPerformAction(context, orderEntryRAO, amount, consumableQuantity, consumableQuantity, isAbsolute);
                maximumQuantity -= consumableQuantity;
            } else {
                isPerformed |= this.alwaysPerformAction(context, orderEntryRAO, amount, maximumQuantity.intValue(), consumableQuantity, isAbsolute);
                maximumQuantity = 0l;
            }
        }

        return isPerformed;
    }

    protected Long getMaximumQuantity(RuleActionContext context) {
        Long maximumQty = (Long) context.getParameter(MAXIMUM_QUANTITY);
        Set<CouponRAO> values = context.getValues(CouponRAO.class);

        if (CollectionUtils.isEmpty(values)) {
            return maximumQty;
        }

        if(maximumQty == null) {
            return null;
        }

        List<CouponRAO> filterCouponRaos = values.stream()
                .filter(rao -> rao.getTotalRedemption() != null)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterCouponRaos)) {
            return maximumQty;
        }

        //One action has only one coupon applied
        if (filterCouponRaos.size() == 1) {
            Long totalRedemption = Long.valueOf(filterCouponRaos.get(0).getTotalRedemption());
            return totalRedemption > maximumQty ? maximumQty : totalRedemption;
        }

        return maximumQty;
    }

    protected void trackConsumedProducts(RuleActionContext context) {
        List<ProductConsumedRAO> productConsumedRAOS = this.lookupRAOObjectsByType(ProductConsumedRAO.class, context);
        if (CollectionUtils.isNotEmpty(productConsumedRAOS)) {
            Iterator var4 = productConsumedRAOS.iterator();

            while (var4.hasNext()) {
                ProductConsumedRAO productConsumedRAO = (ProductConsumedRAO) var4.next();
                OrderEntryRAO orderEntry = productConsumedRAO.getOrderEntry();
                int availableQuantityInOrderEntry = this.ruleEngineCalculationService.getProductAvailableQuantityInOrderEntry(orderEntry);
                productConsumedRAO.setAvailableQuantity(availableQuantityInOrderEntry);
            }

            context.scheduleForUpdate(productConsumedRAOS.toArray());
        }

    }

    protected void trackRuleExecution(RuleActionContext context) {
        String ruleCode = this.getMetaDataFromRule(context, RULE_CODE);
        if (Objects.isNull(ruleCode)) {
            LOGGER.error("cannot track rule execution as current rule:" + context.getRuleName() +
                    " has no rule code defined!");
            return;
        }

        RuleConfigurationRRD config = this.getRuleConfigurationRRD(ruleCode, context);
        if (config != null) {
            config.setCurrentRuns(config.getCurrentRuns() == null ? 1 : config.getCurrentRuns() + 1);
            context.scheduleForUpdate(new Object[]{config});
        }
    }

    protected void trackRuleGroupExecutions(RuleActionContext context) {
        String ruleCode = this.getMetaDataFromRule(context, RULE_CODE);
        if (Objects.nonNull(ruleCode)) {
            RuleConfigurationRRD config = this.getRuleConfigurationRRD(ruleCode, context);
            if (Objects.nonNull(config)) {
                this.trackRuleGroupCode(config.getRuleGroupCode(), context, config);
            }
        } else {
            LOGGER.error("cannot track rule group execution as current rule:"
                    + context.getRuleName() + " has no rule code defined!");
        }

    }

    protected void trackRuleGroupCode(String ruleGroupCode, RuleActionContext context, RuleConfigurationRRD config) {
        if (StringUtils.isNotEmpty(ruleGroupCode)) {
            RuleGroupExecutionRRD execution = this.getRuleGroupExecutionRRD(ruleGroupCode, context);
            if (Objects.nonNull(execution)) {
                this.trackRuleGroupExecution(execution, config);
                context.scheduleForUpdate(new Object[]{execution});
            }
        }

    }

    protected void trackRuleGroupExecution(RuleGroupExecutionRRD execution, RuleConfigurationRRD config) {
        String ruleCode = config.getRuleCode();
        Map<String, Integer> executedRules = execution.getExecutedRules();
        if (Objects.isNull(executedRules)) {
            executedRules = new LinkedHashMap();
            executedRules.put(ruleCode, 1);
            execution.setExecutedRules(executedRules);
        } else {
            Integer ruleCount = executedRules.containsKey(ruleCode) ? executedRules.get(ruleCode) + 1 : 1;
            execution.getExecutedRules().put(ruleCode, ruleCount);
        }

    }

    protected RuleGroupExecutionRRD getRuleGroupExecutionRRD(String ruleGroupCode, RuleActionContext context) {
        return this.lookupRAOByType(RuleGroupExecutionRRD.class, context,
                this.getRuleGroupExecutionRRDFilter(ruleGroupCode)).orElse(null);
    }

    protected Predicate<RuleGroupExecutionRRD> getRuleGroupExecutionRRDFilter(String ruleGroupCode) {
        return (o) -> ruleGroupCode.equals(o.getCode());
    }


    protected RuleConfigurationRRD getRuleConfigurationRRD(String ruleCode, RuleActionContext context) {
        return this.lookupRAOByType(RuleConfigurationRRD.class, context,
                this.getRuleConfigurationRRDFilter(ruleCode)).orElse(null);
    }

    protected Predicate<RuleConfigurationRRD> getRuleConfigurationRRDFilter(String ruleCode) {
        return (o) -> ruleCode.equals(o.getRuleCode());
    }

    protected <T> Optional<T> lookupRAOByType(Class<T> raoType, RuleActionContext context, Predicate... raoFilters) {
        List<T> raoFacts = this.lookupRAOObjectsByType(raoType, context, raoFilters);
        if (raoFacts.size() == 1) {
            return Optional.ofNullable(raoFacts.iterator().next());
        }

        if (CollectionUtils.isEmpty(raoFacts)) {
            LOGGER.error("No RAO facts of type {} are found in the Knowledgebase working memory", raoType.getName());
        } else {
            LOGGER.error("Multiple instances of RAO facts of type {} are found in the Knowledgebase working memory",
                    raoType.getName());
        }

        return Optional.empty();
    }

    protected <T> List<T> lookupRAOObjectsByType(Class<T> raoType, RuleActionContext context, Predicate... raoFilters) {
        KnowledgeHelper helper = this.checkAndGetRuleContext(context);
        Predicate<T> composedRaoFilter = ArrayUtils.isNotEmpty(raoFilters) ? Stream.of(raoFilters)
                .reduce((o) -> true, Predicate::and) : (o) -> true;
        Collection<FactHandle> factHandles = helper.getWorkingMemory()
                .getFactHandles((o) -> raoType.isInstance(o) && composedRaoFilter.test((T) o));
        return CollectionUtils.isNotEmpty(factHandles) ? (List<T>) factHandles.stream().map((h) -> ((InternalFactHandle) h).getObject())
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    protected KnowledgeHelper checkAndGetRuleContext(RuleActionContext context) {
        Object delegate = context.getDelegate();
        Preconditions.checkState(delegate instanceof KnowledgeHelper, "context must be of type org.kie.api.runtime.rule.RuleContext.");
        return (KnowledgeHelper) delegate;
    }

    public void setRAOMetaData(RuleActionContext context, AbstractRuleActionRAO... raos) {
        if (Objects.nonNull(raos)) {
            Stream.of(raos).filter(Objects::nonNull).forEach((r) -> AbstractRuleExecutableSupport.this.addMetadataToRao(r, context));
        }
    }

    protected void addMetadataToRao(AbstractRuleActionRAO rao, RuleActionContext context) {
        rao.setFiredRuleCode(this.getMetaDataFromRule(context, RULE_CODE));
        rao.setModuleName(this.getMetaDataFromRule(context, MODULE_NAME));
        rao.setActionStrategyKey(this.getBeanName());
        rao.setMetadata(this.getMetaDataFromRule(context));
    }

    protected Map<String, String> getMetaDataFromRule(RuleActionContext context) {
        return context.getRuleMetadata().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (e) -> String.valueOf(e.getValue())));
    }

    protected String getMetaDataFromRule(RuleActionContext context, String key) {
        Object value = context.getRuleMetadata().get(key);
        return Objects.isNull(value) ? null : value.toString();
    }

    protected boolean allowedByRuntimeConfiguration(RuleActionContext context) {
        String ruleCode = this.getRuleCode(context);
        RuleConfigurationRRD config = this.getRuleConfigurationRRD(ruleCode, context);
        Optional<RuleGroupExecutionRRD> ruleExecutionRRD = this.lookupRAOByType(RuleGroupExecutionRRD.class, context,
                this.filterByRuleGroup(config));
        boolean allowedToExecute = true;
        if (ruleExecutionRRD.isPresent()) {
            allowedToExecute = ruleExecutionRRD.get().allowedToExecute(config);
        }

        return allowedToExecute;
    }

    protected String getRuleCode(RuleActionContext context) {
        return this.getMetaDataFromRule(context, RULE_CODE);
    }

    protected Predicate<RuleGroupExecutionRRD> filterByRuleGroup(RuleConfigurationRRD config) {
        return e -> e.getCode().equals(config.getRuleGroupCode());
    }

    protected boolean performActionInternal(RuleActionContext context) {
        return true;
    }

    protected void validateRule(RuleActionContext context) {
        if (!validateRuleCode) {
            LOGGER.debug("ignoring validation of rule code. " +
                    "Set 'droolsruleengineservices.validate.droolsrule.rulecode' to true to re-enable validation.");
        } else {
            Map<String, Object> ruleMetaData = context.getRuleMetadata();
            Preconditions.checkState(ruleMetaData.get(RULE_CODE) != null,
                    "rule %s is missing metadata key %s for the rule code.",
                    context.getRuleName(), RULE_CODE);
            if (!validateRulesModuleName) {
                LOGGER.debug("ignoring validation of rules module name. " +
                        "Set 'droolsruleengineservices.validate.droolsrule.modulename' to true to re-enable validation.");
            } else {
                Preconditions.checkState(ruleMetaData.get(MODULE_NAME) != null,
                        "rule %s is missing metadata key %s for the module name.",
                        context.getRulesModuleName(), MODULE_NAME);
            }
        }
    }

    protected Optional<BigDecimal> extractAmountForCurrency(RuleActionContext context, Object currencyAmount) {
        Preconditions.checkArgument(Objects.nonNull(currencyAmount), "The currency-amount map must not be empty: specify at least one CURRENCY->AMOUNT entry.");
        Optional<BigDecimal> amountForCurrency = Optional.empty();
        if (currencyAmount instanceof BigDecimal) {
            amountForCurrency = Optional.ofNullable((BigDecimal) currencyAmount);
        } else if (currencyAmount instanceof Map) {
            Map<String, BigDecimal> currencyAmountMap = (Map) currencyAmount;
            CartRAO cartRao = context.getCartRao();
            amountForCurrency = Objects.nonNull(cartRao) ? Optional.ofNullable(currencyAmountMap.get(cartRao.getCurrencyIsoCode())) :
                    Optional.of((BigDecimal) ((Map.Entry) currencyAmountMap.entrySet().iterator().next()).getValue());
        }

        return amountForCurrency;
    }

    protected int getConsumableQuantity(OrderEntryRAO orderEntryRao) {
        int consumedQuantity = this.getRuleEngineCalculationService().getConsumedQuantityForOrderEntry(orderEntryRao);
        return orderEntryRao.getQuantity() - consumedQuantity;
    }

    protected OrderEntryConsumedRAO consumeOrderEntry(OrderEntryRAO orderEntryRAO, AbstractRuleActionRAO actionRAO) {
        return this.consumeOrderEntry(orderEntryRAO, orderEntryRAO.getQuantity(), this.adjustUnitPrice(orderEntryRAO), actionRAO);
    }

    protected BigDecimal adjustUnitPrice(OrderEntryRAO orderEntryRao) {
        return this.adjustUnitPrice(orderEntryRao, orderEntryRao.getQuantity());
    }

    protected OrderEntryConsumedRAO consumeOrderEntry(OrderEntryRAO orderEntryRAO, int quantity, BigDecimal discountValue, AbstractRuleActionRAO actionRAO) {
        OrderEntryConsumedRAO orderEntryConsumedRAO = this.createOrderEntryConsumedRAO(orderEntryRAO, quantity, discountValue);
        this.updateActionRAOWithConsumed(actionRAO, orderEntryConsumedRAO);
        return orderEntryConsumedRAO;
    }

    protected OrderEntryConsumedRAO createOrderEntryConsumedRAO(OrderEntryRAO orderEntryRAO, int quantity, BigDecimal discountValue) {
        OrderEntryConsumedRAO orderEntryConsumed = new OrderEntryConsumedRAO();
        orderEntryConsumed.setOrderEntry(orderEntryRAO);
        orderEntryConsumed.setQuantity(quantity);
        BigDecimal basePrice = orderEntryRAO.getBasePrice();
        BigDecimal adjustedUnitPrice = basePrice.subtract(discountValue);
//        BigDecimal roundedAdjustedUnitPrice = this.getCurrencyUtils().applyRounding(adjustedUnitPrice, orderEntryRAO.getCurrencyIsoCode());
        orderEntryConsumed.setAdjustedUnitPrice(adjustedUnitPrice);
        return orderEntryConsumed;
    }

    protected void updateActionRAOWithConsumed(AbstractRuleActionRAO actionRAO, OrderEntryConsumedRAO orderEntryConsumedRAO) {
        if (actionRAO != null) {
            Set<OrderEntryConsumedRAO> consumedEntries = actionRAO.getConsumedEntries();
            if (Objects.isNull(consumedEntries)) {
                consumedEntries = Sets.newLinkedHashSet();
                actionRAO.setConsumedEntries(consumedEntries);
            }

            Integer entryNumber = orderEntryConsumedRAO.getOrderEntry().getEntryNumber();
            String firedRuleCode = actionRAO.getFiredRuleCode();
            Optional<OrderEntryConsumedRAO> existingOrderEntryConsumedRAO = consumedEntries.stream()
                    .filter((e) -> e.getOrderEntry().getEntryNumber().equals(entryNumber)
                            && e.getFiredRuleCode().equals(firedRuleCode)).findFirst();
            orderEntryConsumedRAO.setFiredRuleCode(firedRuleCode);
            if (existingOrderEntryConsumedRAO.isPresent()) {
                this.mergeOrderEntryConsumed(existingOrderEntryConsumedRAO.get(), orderEntryConsumedRAO);
            } else {
                consumedEntries.add(orderEntryConsumedRAO);
            }
        }
    }

    protected void mergeOrderEntryConsumed(OrderEntryConsumedRAO consumedTarget, OrderEntryConsumedRAO consumedSource) {
        consumedTarget.setQuantity(consumedTarget.getQuantity() + consumedSource.getQuantity());
        consumedSource.setQuantity(consumedTarget.getQuantity());
    }

    protected BigDecimal adjustUnitPrice(OrderEntryRAO orderEntryRao, int quantity) {
        return this.getRuleEngineCalculationService().getAdjustedUnitPrice(quantity, orderEntryRao);
    }

    public String getBeanName() {
        return beanName;
    }

    protected void validateSelectionStrategy(Collection<EntriesSelectionStrategyRPD> strategies, RuleActionContext context) {
        KnowledgeHelper helper = this.checkAndGetRuleContext(context);
        String ruleName = helper.getRule().getName();
        Preconditions.checkState(CollectionUtils.isNotEmpty(strategies), "rule %s has empty list of entriesSelectionStrategyRPDs.", ruleName);
        AbstractOrderRAO orderRao = null;
        Iterator var7 = strategies.iterator();

        while(var7.hasNext()) {
            EntriesSelectionStrategyRPD strategy = (EntriesSelectionStrategyRPD)var7.next();
            Preconditions.checkState(CollectionUtils.isNotEmpty(strategy.getOrderEntries()), "rule %s has empty order entry list in entriesSelectionStrategyRPDs.", ruleName);
            if (orderRao == null) {
                orderRao = strategy.getOrderEntries().get(0).getOrder();
            }

            Iterator var9 = strategy.getOrderEntries().iterator();

            while(var9.hasNext()) {
                OrderEntryRAO orderEntryRao = (OrderEntryRAO)var9.next();
                Preconditions.checkState(orderEntryRao.getOrder() != null && orderEntryRao.getOrder().equals(orderRao), "rule %s has inconsistent OrderRao in different OrderEntryRao-s of entriesSelectionStrategyRPDs.", ruleName);
            }
        }
    }

    protected boolean hasEnoughQuantity(RuleActionContext context, Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs) {
        Map<Integer, Integer> entriesToBeConsumedMap = this.getEligibleEntryQuantities(selectionStrategyRPDs);
        return selectionStrategyRPDs.stream().flatMap((s) -> s.getOrderEntries().stream())
                .noneMatch((e) -> {
            int consumableQuantity = this.getConsumableQuantity(e);
            return entriesToBeConsumedMap.get(e.getEntryNumber()) > consumableQuantity;
        });
    }

    protected Map<Integer, Integer> getEligibleEntryQuantities(Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs) {
        Map<Integer, Integer> entriesToBeConsumedMap = Maps.newHashMap();
        Iterator var4 = selectionStrategyRPDs.iterator();

        while(var4.hasNext()) {
            EntriesSelectionStrategyRPD strategy = (EntriesSelectionStrategyRPD)var4.next();
            int quantityToBeConsumed = strategy.getQuantity();
            List<OrderEntryRAO> orderEntries = strategy.getOrderEntries();
            int orderEntryCnt = 0;

            Integer entryNumber;
            int eligibleEntryQuantity;
            for(Iterator var9 = orderEntries.iterator(); var9.hasNext(); entriesToBeConsumedMap.put(entryNumber, eligibleEntryQuantity)) {
                OrderEntryRAO orderEntry = (OrderEntryRAO)var9.next();
                ++orderEntryCnt;
                entryNumber = orderEntry.getEntryNumber();
                Integer entryConsumedQty = entriesToBeConsumedMap.get(entryNumber);
                if (Objects.isNull(entryConsumedQty)) {
                    entryConsumedQty = 0;
                }

                if (orderEntryCnt < orderEntries.size()) {
                    int orderEntryConsumableQuantity = this.getConsumableQuantity(orderEntry);
                    int availableOrderEntryQuantity = orderEntryConsumableQuantity - entryConsumedQty;
                    if (availableOrderEntryQuantity <= quantityToBeConsumed) {
                        eligibleEntryQuantity = orderEntryConsumableQuantity;
                        quantityToBeConsumed -= availableOrderEntryQuantity;
                    } else {
                        eligibleEntryQuantity = entryConsumedQty + quantityToBeConsumed;
                        quantityToBeConsumed -= eligibleEntryQuantity;
                    }
                } else {
                    eligibleEntryQuantity = entryConsumedQty + quantityToBeConsumed;
                }
            }
        }

        return entriesToBeConsumedMap;
    }

    protected void splitEntriesSelectionStrategies(List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs,
                                                   List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForAction,
                                                   List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForTriggering) {
        entriesSelectionStrategyRPDs.stream().filter(EntriesSelectionStrategyRPD::isTargetOfAction).forEach(selectionStrategyRPDsForAction::add);
        entriesSelectionStrategyRPDs.stream().filter((s) -> !s.isTargetOfAction()).forEach(selectionStrategyRPDsForTriggering::add);
    }

    protected <T extends AbstractRuleActionRAO> void consumeOrderEntries(Collection<EntriesSelectionStrategyRPD> strategies, T actionRAO) {
        Map<Integer, Integer> selectedOrderEntryMap = this.getSelectedOrderEntryQuantities(strategies);
        Set<OrderEntryRAO> selectedOrderEntryRaos = this.getSelectedOrderEntryRaos(strategies, selectedOrderEntryMap);
        this.consumeOrderEntries(selectedOrderEntryRaos, selectedOrderEntryMap, actionRAO);
    }

    protected Set<OrderEntryConsumedRAO> consumeOrderEntries(Set<OrderEntryRAO> selectedEntries, Map<Integer, Integer> selectedEntriesMap, AbstractRuleActionRAO actionRAO) {
        Set<OrderEntryConsumedRAO> result = Sets.newLinkedHashSet();
        Iterator var6 = selectedEntries.iterator();

        while(var6.hasNext()) {
            OrderEntryRAO selectedEntry = (OrderEntryRAO)var6.next();
            result.add(this.consumeOrderEntry(selectedEntry, selectedEntriesMap.get(selectedEntry.getEntryNumber()), BigDecimal.ZERO, actionRAO));
        }

        return result;
    }

    protected Map<Integer, Integer> getSelectedOrderEntryQuantities(Collection<EntriesSelectionStrategyRPD> strategies) {
        Map<Integer, Integer> result = Maps.newHashMap();
        Iterator var5 = strategies.iterator();

        while(var5.hasNext()) {
            EntriesSelectionStrategyRPD strategy = (EntriesSelectionStrategyRPD)var5.next();
            if (!this.entriesSelectionStrategies.containsKey(strategy.getSelectionStrategy())) {
                throw new IllegalStateException(String.format("UnitForBundleSelector Strategy with identifier '%s' not defined", strategy.getSelectionStrategy()));
            }

            List<OrderEntryRAO> orderEntriesForStrategy = strategy.getOrderEntries();
            Map<Integer, Integer> consumableQtyByOrderEntry = new HashMap<>();
            Iterator var9 = orderEntriesForStrategy.iterator();

            while(var9.hasNext()) {
                OrderEntryRAO orderEntryRAO = (OrderEntryRAO)var9.next();
                int consumableQuantity = this.getConsumableQuantity(orderEntryRAO);
                consumableQtyByOrderEntry.put(orderEntryRAO.getEntryNumber(), consumableQuantity);
            }

            Map<Integer, Integer> consumptionByOrderEntryMap = this.entriesSelectionStrategies.get(strategy.getSelectionStrategy()).pickup(strategy);
            result.putAll(consumptionByOrderEntryMap);
        }

        return result;
    }

    protected boolean adjustStrategyQuantityAndGetTheSameTargetAndTriggerEntry(Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs, int maxCount) {
        int count = 0;
        int totalStrategyQuantity = selectionStrategyRPDs.stream().mapToInt(EntriesSelectionStrategyRPD::getQuantity).sum();
        Iterator var5 = selectionStrategyRPDs.iterator();
        boolean isTheSameTargetAndTriggerEntry = isTheSameTargetAndTriggerEntry(selectionStrategyRPDs);
        while(var5.hasNext()) {
            EntriesSelectionStrategyRPD strategy = (EntriesSelectionStrategyRPD)var5.next();
            int entriesCount = getConsumableEntryCount(strategy);
            int tempCount = isTheSameTargetAndTriggerEntry ? entriesCount / totalStrategyQuantity : entriesCount / strategy.getQuantity();
            if (count == 0) {
                count = tempCount;
            } else {
                count = Math.min(tempCount, count);
            }
        }

        if (maxCount > 0) {
            count = Math.min(count, maxCount);
        }

        final int finalCount = count;
        selectionStrategyRPDs.stream().forEach((s) -> {
            s.setQuantity(s.getQuantity() * finalCount);
        });
        return isTheSameTargetAndTriggerEntry;
    }

    private int getConsumableEntryCount(EntriesSelectionStrategyRPD strategy) {
        int totalConsumableEntryCount = 0;
        for(OrderEntryRAO rao : strategy.getOrderEntries()) {
            if(CollectionUtils.isEmpty(rao.getActions())) {
                totalConsumableEntryCount += rao.getQuantity();
            } else {
                int originQty = rao.getQuantity();
                int consumedQty = 0;
                for(AbstractRuleActionRAO action : rao.getActions()) {
                    if(CollectionUtils.isNotEmpty(action.getConsumedEntries())) {
                        List<OrderEntryConsumedRAO> discountToEntries = action.getConsumedEntries().stream().filter(ce -> ce.getOrderEntry() != null
                                && ce.getOrderEntry().equals(rao)).collect(Collectors.toList());
                        consumedQty = discountToEntries.stream().mapToInt(OrderEntryConsumedRAO::getQuantity).sum();

                    }
                }
                totalConsumableEntryCount += originQty - consumedQty;
            }
        }
        return totalConsumableEntryCount;
    }

    protected boolean isTheSameTargetAndTriggerEntry(Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs) {
        Set<EntriesSelectionStrategyRPD> targetStrategies = selectionStrategyRPDs.stream().filter(s -> s.isTargetOfAction()).collect(Collectors.toSet());
        Set<EntriesSelectionStrategyRPD> triggerStrategies = selectionStrategyRPDs.stream().filter(s -> !s.isTargetOfAction()).collect(Collectors.toSet());
        Set<OrderEntryRAO> targetEntries = targetStrategies.stream().flatMap(ts -> ts.getOrderEntries().stream()).collect(Collectors.toSet());
        Set<OrderEntryRAO> triggerEntries = triggerStrategies.stream().flatMap(ts -> ts.getOrderEntries().stream()).collect(Collectors.toSet());
        if(targetEntries.size() != triggerEntries.size()) {
            return false;
        }

        return triggerEntries.containsAll(targetEntries);
    }

    protected Set<OrderEntryRAO> getSelectedOrderEntryRaos(Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs, Map<Integer, Integer> selectedOrderEntryMap) {
        Set<OrderEntryRAO> orderEntryRAOS = selectionStrategyRPDs.stream()
                .flatMap((selectionStrategy) -> selectionStrategy.getOrderEntries().stream())
                .filter((orderEntry) -> selectedOrderEntryMap.containsKey(orderEntry.getEntryNumber()))
                .collect(Collectors.toSet());
        orderEntryRAOS = orderEntryRAOS.stream().filter((e) -> this.getConsumableQuantity(e) > 0).collect(Collectors.toSet());
        return orderEntryRAOS;
    }

    protected boolean mergeDiscounts(RuleActionContext context, DiscountRAO discountRao, OrderEntryRAO entry) {
        Optional<AbstractRuleActionRAO> actionOptional = entry.getActions().stream()
                .filter((a) -> a instanceof DiscountRAO)
                .filter((a) -> Objects.nonNull(a.getFiredRuleCode()))
                .filter((a) -> a.getFiredRuleCode().equals(context.getRuleName()))
                .findFirst();
        if (actionOptional.isPresent()) {
            DiscountRAO originalDiscount = (DiscountRAO)actionOptional.get();
            originalDiscount.setAppliedToQuantity(originalDiscount.getAppliedToQuantity() + discountRao.getAppliedToQuantity());
        }

        return actionOptional.isPresent();
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Autowired
    public void setRuleEngineCalculationService(RuleEngineCalculationService ruleEngineCalculationService) {
        this.ruleEngineCalculationService = ruleEngineCalculationService;
    }

    public RuleEngineCalculationService getRuleEngineCalculationService() {
        return ruleEngineCalculationService;
    }

    @Value("${droolsruleengineservices.validate.droolsrule.rulecode:true}")
    public void setValidateRuleCode(boolean validateRuleCode) {
        this.validateRuleCode = validateRuleCode;
    }

    @Value("${droolsruleengineservices.validate.droolsrule.moduleName:true}")
    public void setValidateRulesModuleName(boolean validateRulesModuleName) {
        this.validateRulesModuleName = validateRulesModuleName;
    }

    @Autowired
    public void setEntriesSelectionStrategies(Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> entriesSelectionStrategies) {
        this.entriesSelectionStrategies = entriesSelectionStrategies;
    }
}
