package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.dto.PromotionResultData;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.strategy.EcommerceWebFreeGiftFilterStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FreeProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.util.ProductDType;
import com.vctek.orderservice.util.SellSignal;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultEcommerceWebFreeGiftFilterStrategy implements EcommerceWebFreeGiftFilterStrategy {
    private InventoryService inventoryService;
    private RuleConditionsService ruleConditionsService;

    @Override
    public void filterNotSupportFreeGiftComboProduct(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules, List<FreeProductRAO> freeGiftActions, AbstractOrderModel order) {
        if (!SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(order.getSellSignal())) {
            return;
        }
        List<String> validFreeProductRuleCodes = new ArrayList<>();
        Iterator<FreeProductRAO> freeProductRAOIterator = freeGiftActions.iterator();
        while (freeProductRAOIterator.hasNext()) {
            FreeProductRAO freeProductRAO = freeProductRAOIterator.next();
            if (freeProductRAO.getAddedOrderEntry() == null || freeProductRAO.getAddedOrderEntry().getProduct() == null) {
                freeProductRAOIterator.remove();
                continue;
            }
            ProductRAO product = freeProductRAO.getAddedOrderEntry().getProduct();
            if (ProductDType.COMBO_MODEL.code().equalsIgnoreCase(product.getDtype())) {
                freeProductRAOIterator.remove();
            } else {
                validFreeProductRuleCodes.add(freeProductRAO.getFiredRuleCode());
            }
        }

        if (CollectionUtils.isEmpty(validFreeProductRuleCodes)) {
            freeGiftPromotionSourceRules.clear();
            return;
        }

        removeInvalidSourceRules(freeGiftPromotionSourceRules, validFreeProductRuleCodes);
    }

    private void removeInvalidSourceRules(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules, List<String> validFreeProductRuleCodes) {
        Iterator<PromotionSourceRuleModel> iterator = freeGiftPromotionSourceRules.iterator();
        while (iterator.hasNext()) {
            PromotionSourceRuleModel ruleModel = iterator.next();
            if (!validFreeProductRuleCodes.contains(ruleModel.getCode())) {
                iterator.remove();
            }
        }
    }

    @Override
    public AbstractRuleActionRAO filterFreeGiftAppliedAction(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules, List<FreeProductRAO> freeGiftActions, AbstractOrderModel order) {
        if (!SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(order.getSellSignal())) {
            return null;
        }

        Long appliedPromotionSourceRuleId = order.getAppliedPromotionSourceRuleId();
        if (appliedPromotionSourceRuleId != null) {
            FreeProductRAO selectedFreeProductRAO = processSelectPromotionSourceRule(freeGiftPromotionSourceRules, freeGiftActions, order, appliedPromotionSourceRuleId);
            if (selectedFreeProductRAO != null) {
                return selectedFreeProductRAO;
            }
        }

        if (CollectionUtils.isEmpty(freeGiftActions)) {
            freeGiftPromotionSourceRules.clear();
            return null;
        }

        if (freeGiftActions.size() == 1) {
            freeGiftPromotionSourceRules.clear();
            return freeGiftActions.get(0);
        }

        return getFreeProductRAOHasAvailableStockAndFilterSourceRules(freeGiftPromotionSourceRules, freeGiftActions, order);
    }

    private FreeProductRAO getFreeProductRAOHasAvailableStockAndFilterSourceRules(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules,
                                                                                  List<FreeProductRAO> freeGiftActions, AbstractOrderModel order) {
        List<Long> productIds = freeGiftActions.stream().map(a -> a.getAddedOrderEntry().getProduct().getId())
                .collect(Collectors.toList());
        Map<Long, Integer> availableStockMap = inventoryService.getStoreFrontAvailableStockOfProductList(order.getCompanyId(), productIds);
        List<FreeProductRAO> validStockActions = freeGiftActions.stream().filter(action -> {
            Long productId = action.getAddedOrderEntry().getProduct().getId();
            Integer stock = availableStockMap.get(productId);
            return stock != null && stock > 0;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(validStockActions)) {
            List<PromotionResultData> sortedPromotions = ruleConditionsService.sortSourceRulesByOrderTotalCondition(freeGiftPromotionSourceRules);
            freeGiftPromotionSourceRules.clear();
            if(CollectionUtils.isEmpty(sortedPromotions)) {
                return freeGiftActions.get(0);
            }
            PromotionResultData promotionResultData = sortedPromotions.get(0);
            FreeProductRAO freeProductRAO = freeGiftActions.stream().filter(action -> action.getFiredRuleCode().equalsIgnoreCase(promotionResultData.getCode()))
                    .findFirst().orElse(null);
            return freeProductRAO != null ? freeProductRAO : freeGiftActions.get(0);
        }

        List<String> validRuleCodes = validStockActions.stream().map(action -> action.getFiredRuleCode()).collect(Collectors.toList());
        removeInvalidSourceRules(freeGiftPromotionSourceRules, validRuleCodes);

        if (validStockActions.size() == 1) {
            freeGiftPromotionSourceRules.clear();
            return validStockActions.get(0);
        }

        return null;
    }

    private FreeProductRAO processSelectPromotionSourceRule(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules, List<FreeProductRAO> freeGiftActions, AbstractOrderModel order, Long appliedPromotionSourceRuleId) {
        FreeProductRAO selectedFreeProductRAO = getSelectedFreeProductRAO(freeGiftPromotionSourceRules, freeGiftActions, order, appliedPromotionSourceRuleId);

        FreeProductRAO validAppliedProductRao = getFreeProductRAOHasAvailableStockAndFilterSourceRules(freeGiftPromotionSourceRules, freeGiftActions, order);
        if (freeGiftActions.size() == 1) {
            freeGiftPromotionSourceRules.clear();
        }

        if(validAppliedProductRao == null) {
            if(selectedFreeProductRAO == null) {
                order.setAppliedPromotionSourceRuleId(null);
                order.setHasChangeGift(true);
                return null;
            }

            Long selectedFreeProductId = selectedFreeProductRAO.getAddedOrderEntry().getProduct().getId();
            ProductStockData stockData = inventoryService.getStoreFrontStockOfProduct(selectedFreeProductId, order.getCompanyId());
            if(stockData == null || stockData.getQuantity() <= 0) {
                order.setAppliedPromotionSourceRuleId(null);
                order.setHasChangeGift(true);
                return null;
            }

            return selectedFreeProductRAO;
        }

        if(selectedFreeProductRAO == null || !validAppliedProductRao.getFiredRuleCode().equalsIgnoreCase(selectedFreeProductRAO.getFiredRuleCode())) {
            order.setAppliedPromotionSourceRuleId(null);
            order.setHasChangeGift(true);
            return validAppliedProductRao;
        }

        return selectedFreeProductRAO;
    }

    private FreeProductRAO getSelectedFreeProductRAO(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules, List<FreeProductRAO> freeGiftActions, AbstractOrderModel order, Long appliedPromotionSourceRuleId) {
        String selectedRuleCode = freeGiftPromotionSourceRules.stream()
                .filter(r -> r.getId().equals(appliedPromotionSourceRuleId))
                .map(PromotionSourceRuleModel::getCode).findFirst().orElse(null);
        if (selectedRuleCode == null) {
            return null;
        }
        FreeProductRAO freeProductRAO = freeGiftActions.stream()
                .filter(action -> selectedRuleCode.equals(action.getFiredRuleCode())).findFirst().orElse(null);
        if (freeProductRAO == null) {
            return null;
        }
        return freeProductRAO;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Autowired
    public void setRuleConditionsService(RuleConditionsService ruleConditionsService) {
        this.ruleConditionsService = ruleConditionsService;
    }
}
