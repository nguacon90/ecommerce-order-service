package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;
import com.vctek.orderservice.promotionengine.promotionservice.util.PromotionCertainty;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CalculationException;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.repository.CartEntryRepository;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class DefaultPromotionActionService implements PromotionActionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPromotionActionService.class);
    private ModelService modelService;
    private DroolsRuleService droolsRuleService;
    private CalculationService calculationService;
    private CartEntryRepository cartEntryRepository;
    private OrderEntryRepository orderEntryRepository;
    private EntryRepository entryRepository;

    public DefaultPromotionActionService(ModelService modelService, DroolsRuleService droolsRuleService,
                                         CalculationService calculationService) {
        this.modelService = modelService;
        this.droolsRuleService = droolsRuleService;
        this.calculationService = calculationService;
    }

    @Override
    public PromotionResultModel createPromotionResult(AbstractRuleActionRAO actionRao) {
        AbstractOrderModel order = this.getOrderInternal(actionRao);
        if (order == null) {
            AbstractOrderEntryModel orderEntry = this.getOrderEntry(actionRao);
            if (orderEntry != null) {
                order = orderEntry.getOrder();
            }
        }

        DroolsRuleModel engineRule = this.getRule(actionRao);
        PromotionResultModel promoResult = this.findExistingPromotionResultModel(engineRule, order);
        if (Objects.isNull(promoResult)) {
            promoResult = new PromotionResultModel();
        }

        promoResult.setOrder(order);
        if (Objects.nonNull(order)) {
            promoResult.setOrderCode(order.getCode());
        }

        promoResult.setPromotion(this.getPromotion(actionRao, engineRule));
        promoResult.setRulesModuleName(actionRao.getModuleName());
        promoResult.setMessageFired(engineRule != null ? engineRule.getMessageFired() : "");

        Set<PromotionOrderEntryConsumedModel> newConsumedEntries = this.createConsumedEntries(promoResult, actionRao);
        this.createBudgetConsumeModel(promoResult, actionRao);
        if (CollectionUtils.isEmpty(promoResult.getConsumedEntries())) {
            promoResult.setConsumedEntries(newConsumedEntries);
        } else if (CollectionUtils.isNotEmpty(newConsumedEntries)) {
            Set<PromotionOrderEntryConsumedModel> allConsumedEntries = promoResult.getConsumedEntries();
            allConsumedEntries.addAll(newConsumedEntries);
            promoResult.setConsumedEntries(allConsumedEntries);
        }

        if (actionRao instanceof DisplayMessageRAO) {
            promoResult.setCertainty(PromotionCertainty.POTENTIAL.value());
        } else {
            promoResult.setCertainty(PromotionCertainty.FIRED.value());
        }

        return promoResult;
    }

    protected void createBudgetConsumeModel(PromotionResultModel promoResult, AbstractRuleActionRAO actionRao) {
        BudgetConsumedRAO consumedBudget = actionRao.getConsumedBudget();
        if(consumedBudget == null) {
            return;
        }
        PromotionBudgetConsumeModel promotionBudgetConsumeModel = new PromotionBudgetConsumeModel();
        promotionBudgetConsumeModel.setPromotionResult(promoResult);
        promotionBudgetConsumeModel.setOrderCode(consumedBudget.getOrderCode());
        promotionBudgetConsumeModel.setPromotionSourceRuleId(consumedBudget.getPromotionSourceRuleId());
        promotionBudgetConsumeModel.setCustomerId(consumedBudget.getCustomerId());
        promotionBudgetConsumeModel.setMonth(consumedBudget.getMonth());
        promotionBudgetConsumeModel.setYear(consumedBudget.getYear());
        promotionBudgetConsumeModel.setDiscountAmount(consumedBudget.getDiscountAmount().doubleValue());
        promoResult.setBudgetConsumeModel(promotionBudgetConsumeModel);
    }

    @Override
    public DroolsRuleModel getRule(AbstractRuleActionRAO abstractRao) {
        if (Objects.nonNull(abstractRao) && Objects.nonNull(abstractRao.getFiredRuleCode())) {
            String firedRuleCode = abstractRao.getFiredRuleCode();
            return droolsRuleService.findByCodeAndActive(firedRuleCode, true);
        }

        return null;
    }

    @Override
    public void createDiscountValue(DiscountRAO discountRao, String guid, AbstractOrderModel order) {
        boolean isAbsoluteDiscount = discountRao.getCurrencyIsoCode() != null;
        DiscountValue discountValue = new DiscountValue(guid, discountRao.getValue().doubleValue(),
                isAbsoluteDiscount, order.getCurrencyCode());
        List<DiscountValue> globalDVs = new ArrayList(order.getDiscountValues());
        globalDVs.add(discountValue);
        order.setDiscountValues(globalDVs);
        order.setCalculated(Boolean.FALSE);
    }

    @Override
    public void recalculateTotals(AbstractOrderModel order) {
        try {
            this.calculationService.calculateTotals(order, true);
        } catch (CalculationException var3) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(var3.getMessage(), var3);
            }

            order.setCalculated(Boolean.FALSE);
            this.modelService.save(order);
        }

    }

    @Override
    public List<ItemModel> removeDiscountValue(String guid, AbstractOrderModel order) {
        List<ItemModel> modifiedItems = new LinkedList();
        if (this.removeOrderLevelDiscount(guid, order)) {
            modifiedItems.add(order);
        }

        modifiedItems.addAll(this.removeOrderEntryLevelDiscounts(guid, order));
        return modifiedItems;
    }

    protected List<ItemModel> removeOrderEntryLevelDiscounts(String code, AbstractOrderModel order) {
        return (List)order.getEntries().stream()
                .filter((entry) -> this.removeOrderEntryLevelDiscount(code, entry))
                .collect(Collectors.toList());
    }

    protected boolean removeOrderLevelDiscount(String code, AbstractOrderModel order) {
        //FIXME remove all discount values to recalculate. Actually only promotion discount value
        order.setDiscountValues(Collections.emptyList());
        return true;
        //return this.removeDiscount(code, order.getDiscountValues(), (dvs) -> order.setDiscountValues(dvs));
    }

    protected boolean removeOrderEntryLevelDiscount(String code, AbstractOrderEntryModel orderEntry) {
        //FIXME remove all discount values to recalculate. Actually only promotion discount value
        orderEntry.setDiscountValues(Collections.emptyList());
        return true;
//        return this.removeDiscount(code, orderEntry.getDiscountValues(), (dvs) -> {
//            orderEntry.setDiscountValues(dvs);
//        });
    }

    protected boolean removeDiscount(String code, List<DiscountValue> discountValuesList, Consumer<List<DiscountValue>> setNewDiscountValues) {
        List<DiscountValue> filteredDVs = discountValuesList.stream()
                .filter((dv) -> !code.equals(dv.getCode()))
                .collect(Collectors.toList());
        boolean changed = filteredDVs.size() != discountValuesList.size();
        if (setNewDiscountValues != null && changed) {
            setNewDiscountValues.accept(filteredDVs);
        }

        return changed;
    }

    protected AbstractOrderModel getOrderInternal(AbstractRuleActionRAO action) {
        Preconditions.checkArgument(action != null, "action rao must not be null");
        AbstractOrderRAO orderRao = null;
        if (action.getAppliedToObject() instanceof OrderEntryRAO) {
            OrderEntryRAO entry = (OrderEntryRAO) action.getAppliedToObject();
            orderRao = entry.getOrder();
        } else if (action.getAppliedToObject() instanceof AbstractOrderRAO) {
            orderRao = (AbstractOrderRAO) action.getAppliedToObject();
        }

        return orderRao != null ? this.getOrder(orderRao) : null;
    }


    protected AbstractOrderModel getOrder(AbstractOrderRAO orderRao) {
        return this.modelService.findById(AbstractOrderModel.class, orderRao.getId());
    }

    public AbstractOrderModel getOrder(AbstractRuleActionRAO action) {
        AbstractOrderModel order = this.getOrderInternal(action);
        if (order == null) {
            LOGGER.error("cannot look-up order for action:" + action.toString());
        }

        return order;
    }


    @Override
    public AbstractOrderEntryModel getOrderEntry(AbstractRuleActionRAO action) {
        Preconditions.checkArgument(action != null, "action must not be null");
        return !(action.getAppliedToObject() instanceof OrderEntryRAO) ? null :
                this.getOrderEntry((OrderEntryRAO) action.getAppliedToObject());
    }

    @Override
    public void createDiscountValue(DiscountRAO discountRao, String code, AbstractOrderEntryModel orderEntry) {
        boolean isAbsoluteDiscount = Objects.nonNull(discountRao.getCurrencyIsoCode());
        DiscountValue discountValue = new DiscountValue(code, discountRao.getValue().doubleValue(), isAbsoluteDiscount,
                orderEntry.getOrder().getCurrencyCode());
        List<DiscountValue> globalDVs = new ArrayList(orderEntry.getDiscountValues());
        globalDVs.add(discountValue);
        orderEntry.setDiscountValues(globalDVs);
        orderEntry.setCalculated(Boolean.FALSE);
    }

    @Override
    public void removeDiscountValueBy(List<String> actionGuids, AbstractOrderModel order) {
        this.removeDiscountBy(actionGuids, order.getDiscountValues(), (dvs) -> order.setDiscountValues(dvs));
        List<AbstractOrderEntryModel> entries = entryRepository.findAllByOrder(order);

        entries.stream().forEach(
                (entry) -> this.removeDiscountBy(actionGuids, entry.getDiscountValues(),
                        (dvs) -> entry.setDiscountValues(dvs)));
        modelService.save(order);
        modelService.saveAll(entries);
    }

    protected boolean removeDiscountBy(List<String> codes, List<DiscountValue> discountValuesList, Consumer<List<DiscountValue>> setNewDiscountValues) {
        List<DiscountValue> filteredDVs = discountValuesList.stream()
                .filter((dv) -> !codes.contains(dv.getCode()))
                .collect(Collectors.toList());
        boolean changed = filteredDVs.size() != discountValuesList.size();
        if (setNewDiscountValues != null && changed) {
            setNewDiscountValues.accept(filteredDVs);
        }

        return changed;
    }

    protected AbstractOrderEntryModel getOrderEntry(OrderEntryRAO orderEntryRao) {
        Preconditions.checkArgument(orderEntryRao != null, "orderEntryRao must not be null");
        Preconditions.checkArgument(orderEntryRao.getEntryNumber() != null, "orderEntryRao.entryNumber must not be null");
        Preconditions.checkArgument(orderEntryRao.getProduct() != null, "orderEntryRao.product must not be null");
        Preconditions.checkArgument(orderEntryRao.getProduct().getId() != null, "orderEntryRao.product.code must not be null");

        AbstractOrderModel order = this.getOrder(orderEntryRao.getOrder());
        if (order == null) {
            return null;
        }
        if(order instanceof CartModel) {
            return cartEntryRepository.findByOrderAndEntryNumber(order, orderEntryRao.getEntryNumber());
        } else {
            return orderEntryRepository.findByOrderAndEntryNumber((OrderModel) order, orderEntryRao.getEntryNumber());
        }
    }

    protected PromotionResultModel findExistingPromotionResultModel(DroolsRuleModel rule, AbstractOrderModel order) {
        if (rule != null && order != null) {
            Set<PromotionResultModel> results = order.getPromotionResults();
            Iterator var5 = results.iterator();

            while (var5.hasNext()) {
                PromotionResultModel result = (PromotionResultModel) var5.next();
                Collection<AbstractPromotionActionModel> actions = result.getActions();
                boolean isMatch = actions.stream()
                        .filter((a) -> a instanceof AbstractRuleBasedPromotionActionModel)
                        .map((a) -> (AbstractRuleBasedPromotionActionModel) a)
                        .anyMatch((a) -> a.getRule() != null && rule.getId().equals(a.getRule().getId()));
                if (isMatch) {
                    return result;
                }
            }
        }

        return null;
    }

    protected RuleBasedPromotionModel getPromotion(AbstractRuleActionRAO abstractRao, DroolsRuleModel engineRule) {
        RuleBasedPromotionModel promotionModel = null;
        if (Objects.nonNull(abstractRao) && Objects.nonNull(abstractRao.getFiredRuleCode())
                && engineRule != null) {
            promotionModel = engineRule.getPromotion();
        } else {
            String firedRuleCode = abstractRao != null ? abstractRao.getFiredRuleCode() : StringUtils.EMPTY;
            LOGGER.error("Cannot get promotion for AbstractRuleActionRAO: {}. No rule found for code: {}",
                    abstractRao, firedRuleCode);
        }
        return promotionModel;
    }

    protected Set<PromotionOrderEntryConsumedModel> createConsumedEntries(PromotionResultModel promoResult, AbstractRuleActionRAO action) {
        Set<PromotionOrderEntryConsumedModel> promotionOrderEntriesConsumed = new HashSet<>();
        if (Objects.nonNull(action) && Objects.nonNull(action.getConsumedEntries())) {
            String firedRuleCode = action.getFiredRuleCode();
            List<OrderEntryConsumedRAO> orderEntryConsumedRAOsForRule = action.getConsumedEntries().stream()
                    .filter(oec -> oec.getFiredRuleCode() != null
                            && oec.getFiredRuleCode().equals(firedRuleCode))
                    .collect(Collectors.toList());

            PromotionOrderEntryConsumedModel promotionOrderEntryConsumed;
            for(Iterator var5 = orderEntryConsumedRAOsForRule.iterator(); var5.hasNext(); promotionOrderEntriesConsumed.add(promotionOrderEntryConsumed)) {
                OrderEntryConsumedRAO orderEntryConsumedRAO = (OrderEntryConsumedRAO)var5.next();
                promotionOrderEntryConsumed = new PromotionOrderEntryConsumedModel();
                AbstractOrderEntryModel orderEntry = this.getOrderEntry(orderEntryConsumedRAO.getOrderEntry());
                promotionOrderEntryConsumed.setOrderEntry(orderEntry);
                promotionOrderEntryConsumed.setOrderEntryNumber(orderEntry.getEntryNumber());
                promotionOrderEntryConsumed.setQuantity((long)orderEntryConsumedRAO.getQuantity());
                promotionOrderEntryConsumed.setPromotionResult(promoResult);
                long appliedQty = calculateAppliedQuantity(orderEntryConsumedRAO, firedRuleCode);
                promotionOrderEntryConsumed.setAppliedQuantity(appliedQty);
                if (orderEntryConsumedRAO.getAdjustedUnitPrice() != null) {
                    promotionOrderEntryConsumed.setAdjustedUnitPrice(orderEntryConsumedRAO.getAdjustedUnitPrice().doubleValue());
                }
            }
        }

        return promotionOrderEntriesConsumed;
    }

    private long calculateAppliedQuantity(OrderEntryConsumedRAO orderEntryConsumedRAO, String firedRuleCode) {
        OrderEntryRAO orderEntry = orderEntryConsumedRAO.getOrderEntry();
        if(orderEntry == null) {
            return orderEntryConsumedRAO.getQuantity();
        }

        if(CollectionUtils.isEmpty(orderEntry.getActions())) {
            return orderEntryConsumedRAO.getQuantity();
        }

        List<DiscountRAO> discountActions = orderEntry.getActions().stream()
                .filter(action -> action instanceof DiscountRAO && action.getFiredRuleCode().equals(firedRuleCode))
                .map(a -> (DiscountRAO) a).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(discountActions)) {
            return orderEntryConsumedRAO.getQuantity();
        }

        return discountActions.stream().mapToLong(d -> {
            if(d.isTheSamePartnerProduct()) {
                return getConsumedQuantityOf(d, firedRuleCode);
            }
            return CommonUtils.readValue(d.getAppliedToQuantity());
        }).sum();
    }

    public long getConsumedQuantityOf(DiscountRAO rao, String fireRuleCode) {
        if(CollectionUtils.isEmpty(rao.getConsumedEntries())) {
            return CommonUtils.readValue(rao.getAppliedToQuantity());
        }

        List<OrderEntryConsumedRAO> consumedEntries = rao.getConsumedEntries().stream().filter(e -> fireRuleCode.equals(e.getFiredRuleCode()))
                .collect(Collectors.toList());
        return consumedEntries.stream().mapToInt(ce -> CommonUtils.readValue(ce.getQuantity())).sum();
    }

    @Autowired
    public void setCartEntryRepository(CartEntryRepository cartEntryRepository) {
        this.cartEntryRepository = cartEntryRepository;
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }
}
