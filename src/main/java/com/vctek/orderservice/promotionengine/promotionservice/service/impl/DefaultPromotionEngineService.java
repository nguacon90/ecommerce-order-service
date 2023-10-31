package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.result.PromotionOrderResults;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionEngineService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionsService;
import com.vctek.orderservice.promotionengine.promotionservice.strategy.EcommerceWebFreeGiftFilterStrategy;
import com.vctek.orderservice.promotionengine.promotionservice.strategy.impl.DefaultAddProductToCartActionStrategy;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationResult;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleEngineRuntimeException;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsRuleEngineContextRepository;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import com.vctek.orderservice.promotionengine.ruleengine.service.RuleEngineService;
import com.vctek.orderservice.promotionengine.ruleengineservice.action.RuleActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CalculationException;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.FactContextFactory;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.FactContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.FactContextType;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleActionStrategy;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component("promotionEngineService")
public class DefaultPromotionEngineService implements PromotionEngineService, PromotionsService {
    public static final Long DEFAULT_RULE_FIRING_LIMIT = 200l;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPromotionEngineService.class);
    private DroolsRuleEngineContextRepository contextRepository;
    private RuleEngineService ruleEngineService;
    private FactContextFactory factContextFactory;
    private ModelService modelService;
    private RuleActionService ruleActionService;
    private CalculationService calculationService;
    private List<RuleActionStrategy> strategies;
    private PromotionSourceRuleService promotionSourceRuleService;
    private PromotionResultService promotionResultService;
    private DefaultAddProductToCartActionStrategy defaultAddProductToCartActionStrategy;
    private DroolsKIEModuleService droolsKIEModuleService;
    private CustomerService customerService;
    private EcommerceWebFreeGiftFilterStrategy ecommerceWebFreeGiftFilterStrategy;

    public DefaultPromotionEngineService(DroolsRuleEngineContextRepository contextRepository,
                                         @Qualifier("droolsRuleEngineService") RuleEngineService ruleEngineService,
                                         FactContextFactory factContextFactory,
                                         @Qualifier("promotionRuleActionService") RuleActionService ruleActionService,
                                         CalculationService calculationService,
                                         PromotionSourceRuleService promotionSourceRuleService) {
        this.contextRepository = contextRepository;
        this.ruleEngineService = ruleEngineService;
        this.factContextFactory = factContextFactory;
        this.ruleActionService = ruleActionService;
        this.calculationService = calculationService;
        this.promotionSourceRuleService = promotionSourceRuleService;
    }

    @Override
    public synchronized PromotionOrderResults updatePromotions(Collection<PromotionGroupModel> promotionGroups, AbstractOrderModel order) {
        return this.updatePromotionsNotThreadSafe(promotionGroups, order, Calendar.getInstance().getTime());
    }

    @Override
    public RuleEvaluationResult evaluate(AbstractOrderModel order, Collection<PromotionGroupModel> groups, Date date) {
        List<Object> facts = new ArrayList<>();
        facts.add(order);
        facts.addAll(groups);
        if (Objects.nonNull(date)) {
            facts.add(date);
        }

        try {
            FactContext factContext = this.factContextFactory.createFactContext(FactContextType.PROMOTION_ORDER, facts);
            RuleEvaluationContext context = this.prepareContext(factContext, this.determineRuleEngineContext(order));
            return this.ruleEngineService.evaluate(context);
        } catch (RuntimeException e) {
            LOGGER.error("Promotion rule evaluation failed: orderCode: " + order.getCode(), e);
            RuleEvaluationResult result = new RuleEvaluationResult();
            result.setErrorMessage(e.getMessage());
            result.setEvaluationFailed(true);
            return result;
        }
    }

    @Override
    public RuleEngineResultRAO doEvaluateCartTemp(CartModel cartModel) {
        try {
            RuleEvaluationResult evaluationResult = this.evaluate(cartModel, Collections.emptyList(), Calendar.getInstance().getTime());
            if (!evaluationResult.isEvaluationFailed()) {
                RuleEngineResultRAO ruleEngineResultRAO = evaluationResult.getResult();
                filterActionsToApplyByPriority(cartModel, ruleEngineResultRAO);
                return ruleEngineResultRAO;
            }
        } catch (RuleEngineRuntimeException var8) {
            LOGGER.error(var8.getMessage(), var8);
        } catch (RuntimeException var8) {
            LOGGER.error(var8.getMessage(), var8);
        }
        return null;
    }

    private PromotionOrderResults updatePromotionsNotThreadSafe(Collection<PromotionGroupModel> promotionGroups, AbstractOrderModel order, Date date) {
        this.cleanupAbstractOrder(order);
        List<PromotionResultModel> applyAllActionsPromotionResults = new ArrayList<>();
        boolean customerLimitedApplyPromotion = customerService.limitedApplyPromotionAndReward(order.getCustomerId(), order.getCompanyId());
        if (customerLimitedApplyPromotion) {
            return new PromotionOrderResults(order, applyAllActionsPromotionResults, 0.0D);
        }

        try {
            RuleEvaluationResult ruleEvaluationResult = this.evaluate(order, promotionGroups, date);
            if (!ruleEvaluationResult.isEvaluationFailed()) {
                RuleEngineResultRAO ruleEngineResultRAO = ruleEvaluationResult.getResult();
                filterActionsToApplyByPriority(order, ruleEngineResultRAO);
                List<ItemModel> applyAllActionModels = this.ruleActionService.applyAllActions(ruleEngineResultRAO);
                applyAllActionsPromotionResults.addAll(applyAllActionModels.stream()
                        .filter((item) -> item instanceof PromotionResultModel)
                        .map((item) -> (PromotionResultModel) item).collect(Collectors.toList()));
            }
        } catch (RuleEngineRuntimeException var8) {
            LOGGER.error(var8.getMessage(), var8);
        } catch (RuntimeException var8) {
            LOGGER.error(var8.getMessage(), var8);
        }

        return new PromotionOrderResults(order, applyAllActionsPromotionResults, 0.0D);
    }

    protected void filterActionsToApplyByPriority(AbstractOrderModel order, RuleEngineResultRAO ruleEngineResultRAO) {
        List<AbstractRuleActionRAO> actions = ruleEngineResultRAO.getActions().stream().collect(Collectors.toList());
        Map<String, PromotionSourceRuleModel> sourceRuleModelMap = new HashMap<>();
        Set<PromotionSourceRuleModel> existedCouldFirePromotions = order.getCouldFirePromotions();
        order.getCouldFirePromotions().removeAll(existedCouldFirePromotions);
        if (CollectionUtils.isEmpty(actions)) {
            order.setAppliedPromotionSourceRuleId(null);
            return;
        }

        Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules = new HashSet<>();
        List<AbstractRuleActionRAO> fixedDiscountActions = new ArrayList<>();
        List<AbstractRuleActionRAO> discountActions = new ArrayList<>();
        List<FreeProductRAO> freeGiftActions = new ArrayList<>();

        AbstractRuleActionRAO appliedOnlyOnePromotion = filterAppliedOnlyOnePromotion(actions);
        if(appliedOnlyOnePromotion != null && !(appliedOnlyOnePromotion instanceof FreeProductRAO)) {
            discountActions.add(appliedOnlyOnePromotion);
            ruleEngineResultRAO.setActions(new LinkedHashSet<>(discountActions));
            return;
        }

        populateDiscountActions(actions, sourceRuleModelMap, freeGiftPromotionSourceRules, fixedDiscountActions, discountActions, freeGiftActions);

        List<FixedPriceProductRAO> fixedPriceProductRAOS = filterCheapestFixedPriceProductAction(fixedDiscountActions);
        discountActions.addAll(fixedPriceProductRAOS);

        if(CollectionUtils.isEmpty(freeGiftPromotionSourceRules)) {
            ruleEngineResultRAO.setActions(new LinkedHashSet<>(discountActions));
            return;
        }

        ecommerceWebFreeGiftFilterStrategy.filterNotSupportFreeGiftComboProduct(freeGiftPromotionSourceRules, freeGiftActions, order);
        AbstractRuleActionRAO freeGiftAppliedAction;
        if(SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(order.getSellSignal())) {
            freeGiftAppliedAction = ecommerceWebFreeGiftFilterStrategy.filterFreeGiftAppliedAction(freeGiftPromotionSourceRules, freeGiftActions, order);
        } else {
            freeGiftAppliedAction = getFreeGiftAppliedAction(order, freeGiftPromotionSourceRules, freeGiftActions);
        }

        if (freeGiftAppliedAction == null) {
            ruleEngineResultRAO.setActions(new LinkedHashSet<>(discountActions));
            order.getCouldFirePromotions().addAll(freeGiftPromotionSourceRules);
            defaultAddProductToCartActionStrategy.removeProductInComboOfOrder(order.getCode());
            return;
        }

        AbstractRuleActionRAO freeGiftAppliedOnlyActionRAO = this.filterAppliedOnlyOnePromotion(Arrays.asList(freeGiftAppliedAction));
        if(freeGiftAppliedOnlyActionRAO != null) {
            List<AbstractRuleActionRAO> freeGiftAppliedOnlyList = new ArrayList<>();
            freeGiftAppliedOnlyList.add(freeGiftAppliedOnlyActionRAO);
            order.getCouldFirePromotions().addAll(freeGiftPromotionSourceRules);
            ruleEngineResultRAO.setActions(new LinkedHashSet<>(freeGiftAppliedOnlyList));
            return;
        }

        order.getCouldFirePromotions().addAll(freeGiftPromotionSourceRules);
        discountActions.add(freeGiftAppliedAction);
        ruleEngineResultRAO.setActions(new LinkedHashSet<>(discountActions));
    }

    private void populateDiscountActions(List<AbstractRuleActionRAO> actions, Map<String, PromotionSourceRuleModel> sourceRuleModelMap,
                                         Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules,
                                         List<AbstractRuleActionRAO> fixedDiscountAction,
                                         List<AbstractRuleActionRAO> discountActions, List<FreeProductRAO> freeGiftActions) {
        for (AbstractRuleActionRAO action : actions) {
            if (action instanceof FreeProductRAO) {
                String firedRuleCode = action.getFiredRuleCode();
                PromotionSourceRuleModel sourceRuleModel = sourceRuleModelMap.get(firedRuleCode);
                if (sourceRuleModel == null) {
                    sourceRuleModel = promotionSourceRuleService.findByCode(firedRuleCode);
                    sourceRuleModelMap.put(firedRuleCode, sourceRuleModel);
                }
                freeGiftPromotionSourceRules.add(sourceRuleModel);
                freeGiftActions.add((FreeProductRAO) action);
            } else if(action instanceof FixedPriceProductRAO) {
                fixedDiscountAction.add(action);
            } else {
                discountActions.add(action);
            }
        }
    }

    private AbstractRuleActionRAO filterAppliedOnlyOnePromotion(List<AbstractRuleActionRAO> actions) {
        List<String> firedRuleCodes = actions.stream().map(a -> a.getFiredRuleCode()).collect(Collectors.toList());
        List<PromotionSourceRuleModel> allAppliedOnlyOnePromotions = promotionSourceRuleService.findAllByAppliedOnlyOneAndCodeIn(true, firedRuleCodes);
        if(CollectionUtils.isEmpty(allAppliedOnlyOnePromotions)) {
            return null;
        }

        Collections.sort(allAppliedOnlyOnePromotions, (o1, o2) -> CommonUtils.readValue(o2.getPriority()) > CommonUtils.readValue(o1.getPriority()) ? 1 : -1);
        PromotionSourceRuleModel sourceRuleModel = allAppliedOnlyOnePromotions.get(0);
        Optional<AbstractRuleActionRAO> optional = actions.stream()
                .filter(a -> a.getFiredRuleCode().equals(sourceRuleModel.getCode())).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }

    protected List<FixedPriceProductRAO> filterCheapestFixedPriceProductAction(List<AbstractRuleActionRAO> fixedPriceProductActions) {
        List<FixedPriceProductRAO> fixedPriceActions = fixedPriceProductActions.stream()
                .filter(action -> {
                    if (action instanceof FixedPriceProductRAO) {
                        FixedPriceProductRAO fixedPriceProductRAO = (FixedPriceProductRAO) action;
                        return fixedPriceProductRAO.getFixedPrice() != null
                                && StringUtils.isNotBlank(fixedPriceProductRAO.getFiredRuleCode());
                    }

                    return false;
                })
                .map(action -> (FixedPriceProductRAO) action)
                .collect(Collectors.toList());
        Map<Long, FixedPriceProductRAO> cheapestAppliedToEntry = new HashMap<>();
        if (CollectionUtils.isNotEmpty(fixedPriceActions)) {
            for (FixedPriceProductRAO action : fixedPriceActions) {
                BigDecimal fixedPrice = action.getFixedPrice();
                OrderEntryRAO orderEntryRAO = action.getOrderEntryRAO();
                Long entryId = orderEntryRAO.getId();
                FixedPriceProductRAO fixedPriceProductRAO = cheapestAppliedToEntry.get(entryId);
                if (fixedPriceProductRAO == null) {
                    cheapestAppliedToEntry.put(entryId, action);
                    continue;
                }
                BigDecimal currentFixedPrice = fixedPriceProductRAO.getFixedPrice();
                if (currentFixedPrice.doubleValue() > fixedPrice.doubleValue()) {
                    cheapestAppliedToEntry.put(entryId, action);
                }
            }
            return cheapestAppliedToEntry.values().stream().collect(Collectors.toList());

        }

        return new ArrayList<>();
    }

    private AbstractRuleActionRAO getFreeGiftAppliedAction(AbstractOrderModel order,
                                                           Set<PromotionSourceRuleModel> freeGiftRules,
                                                           List<FreeProductRAO> highPriorityActions) {

        Long appliedPromotionSourceRuleId = order.getAppliedPromotionSourceRuleId();
        if (appliedPromotionSourceRuleId == null) {
            return null;
        }

        Optional<String> ruleCodeOptional = freeGiftRules.stream()
                .filter(r -> r.getId().equals(appliedPromotionSourceRuleId))
                .map(PromotionSourceRuleModel::getCode).findFirst();
        if (!ruleCodeOptional.isPresent()) {
            order.setAppliedPromotionSourceRuleId(null);
            return null;
        }

        String ruleCode = ruleCodeOptional.get();
        Optional<FreeProductRAO> firstRuleCodeOptional = highPriorityActions.stream()
                .filter(action -> ruleCode.equals(action.getFiredRuleCode())).findFirst();

        if (!firstRuleCodeOptional.isPresent()) {
            order.setAppliedPromotionSourceRuleId(null);
            return null;
        }

        return firstRuleCodeOptional.get();
    }

    public void cleanupAbstractOrder(AbstractOrderModel order) {
        List<PromotionResultModel> promotionResultModels = promotionResultService.findAllByOrder(order);
        if (CollectionUtils.isNotEmpty(promotionResultModels)) {
            Iterator var4 = promotionResultModels.iterator();
            while (var4.hasNext()) {
                PromotionResultModel promotionResultModel = (PromotionResultModel) var4.next();
                Set<AbstractPromotionActionModel> actions = promotionResultModel.getActions().stream()
                        .collect(Collectors.toSet());
                Iterator var6 = actions.iterator();

                while (var6.hasNext()) {
                    AbstractPromotionActionModel action = (AbstractPromotionActionModel) var6.next();
                    if (action instanceof AbstractRuleBasedPromotionActionModel) {
                        this.undoPromotionAction(action);
                    }
                }
                undoConsumedEntries(promotionResultModel);
                promotionResultModel.getActions().removeAll(actions);
            }

            order.getPromotionResults().removeAll(promotionResultModels);
            try {
                this.getModelService().save(order);
                this.recalculateCart(order);
            } catch (ObjectOptimisticLockingFailureException e) {
                LOGGER.error(e.getMessage());
                this.getModelService().save(order);
                this.recalculateCart(order);
            }
        }
    }

    private void undoConsumedEntries(PromotionResultModel promotionResultModel) {
        Set<PromotionOrderEntryConsumedModel> consumedModels = promotionResultModel.getConsumedEntries()
                .stream().collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(consumedModels)) {
            return;
        }

        consumedModels.stream()
                .filter(cse -> cse.getOrderEntry() != null && CollectionUtils.isNotEmpty(cse.getOrderEntry().getConsumedEntries()))
                .forEach(cse -> cse.getOrderEntry().setConsumedEntries(new HashSet<>()));

        promotionResultModel.getConsumedEntries().removeAll(consumedModels);
    }

    protected boolean recalculateCart(AbstractOrderModel order) {
        try {
            this.calculationService.calculateTotals(order, true);
            return true;
        } catch (CalculationException var3) {
            LOGGER.error(String.format("Recalculation of order with code '%s' failed.", order.getCode()), var3);
            order.setCalculated(Boolean.FALSE);
            this.getModelService().save(order);
            return false;
        }
    }

    protected void undoPromotionAction(AbstractPromotionActionModel action) {
        AbstractRuleBasedPromotionActionModel promoAction = (AbstractRuleBasedPromotionActionModel) action;
        RuleActionStrategy ruleActionStrategy = this.getRuleActionStrategy(promoAction.getStrategyId());
        if (ruleActionStrategy != null) {
            ruleActionStrategy.undo(promoAction);
        }
    }

    protected RuleActionStrategy getRuleActionStrategy(String strategyId) {
        if (strategyId == null) {
            LOGGER.error("strategyId is not defined!");
            return null;
        } else {
            if (this.getStrategies() != null) {
                Iterator var3 = this.getStrategies().iterator();

                while (var3.hasNext()) {
                    RuleActionStrategy strategy = (RuleActionStrategy) var3.next();
                    if (strategyId.equals(strategy.getStrategyId())) {
                        return strategy;
                    }
                }

                LOGGER.error("cannot find RuleActionStrategy for given strategyId:{}", strategyId);
            } else {
                LOGGER.error("cannot call getRuleActionStrategy(\"{}\"), no strategies are defined! Please configure your {} bean to contain strategies.", strategyId, this.getClass().getSimpleName());
            }

            return null;
        }
    }

    private RuleEvaluationContext prepareContext(FactContext factContext, DroolsRuleEngineContextModel ruleEngineContext) {
        Set<Object> convertedFacts = this.provideRAOs(factContext);
        RuleEvaluationContext evaluationContext = new RuleEvaluationContext();
        evaluationContext.setRuleEngineContext(ruleEngineContext);
        evaluationContext.setFacts(convertedFacts);
        return evaluationContext;
    }

    protected Set<Object> provideRAOs(FactContext factContext) {
        Set<Object> result = new HashSet();
        Iterator var4 = factContext.getFacts().iterator();

        while (var4.hasNext()) {
            Object fact = var4.next();
            Iterator var6 = factContext.getProviders(fact).iterator();

            while (var6.hasNext()) {
                RAOProvider raoProvider = (RAOProvider) var6.next();
                result.addAll(raoProvider.expandFactModel(fact));
            }
        }

        return result;
    }

    protected DroolsRuleEngineContextModel determineRuleEngineContext(AbstractOrderModel order) {
        Long companyId = order.getCompanyId();
        DroolsKIEModuleModel kieModuleModel = droolsKIEModuleService.findByCompanyId(companyId);
        if(kieModuleModel == null) {
            throw new IllegalArgumentException("Cannot find kie module for company: " + companyId);
        }
        DroolsKIEBaseModel defaultKIEBase = kieModuleModel.getDefaultKIEBase();
        if(defaultKIEBase == null) {
            throw new IllegalArgumentException("Cannot find kie base for company: " + companyId);
        }
        return contextRepository.findByKieSession(defaultKIEBase.getDefaultKieSession());
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public List<RuleActionStrategy> getStrategies() {
        return strategies;
    }

    @Autowired
    @Qualifier("actionStrategies")
    public void setStrategies(List<RuleActionStrategy> strategies) {
        this.strategies = strategies;
    }

    @Autowired
    public void setPromotionResultService(PromotionResultService promotionResultService) {
        this.promotionResultService = promotionResultService;
    }

    @Autowired
    public void setDefaultAddProductToCartActionStrategy(DefaultAddProductToCartActionStrategy defaultAddProductToCartActionStrategy) {
        this.defaultAddProductToCartActionStrategy = defaultAddProductToCartActionStrategy;
    }

    @Autowired
    public void setDroolsKIEModuleService(DroolsKIEModuleService droolsKIEModuleService) {
        this.droolsKIEModuleService = droolsKIEModuleService;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setEcommerceWebFreeGiftFilterStrategy(EcommerceWebFreeGiftFilterStrategy ecommerceWebFreeGiftFilterStrategy) {
        this.ecommerceWebFreeGiftFilterStrategy = ecommerceWebFreeGiftFilterStrategy;
    }
}
