package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractPromotionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedPromotionModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionOrderEntryConsumedRepository;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionResultRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultPromotionResultService implements PromotionResultService {
    private PromotionResultRepository promotionResultRepository;
    private PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository;
    private RuleActionsRegistry ruleActionsRegistry;
    private RuleActionsService ruleActionsService;
    private RuleConditionsService ruleConditionsService;
    private RuleConditionsRegistry ruleConditionsRegistry;

    public DefaultPromotionResultService(PromotionResultRepository promotionResultRepository) {
        this.promotionResultRepository = promotionResultRepository;
    }

    @Override
    public String getDescription(PromotionResultModel promoResult) {
        return promoResult.getMessageFired();
    }

    @Override
    public List<PromotionResultModel> findAllByOrder(AbstractOrderModel order) {
        return promotionResultRepository.findAllByOrder(order);
    }

    @Override
    public Set<PromotionSourceRuleModel> findAllPromotionSourceRulesByOrder(AbstractOrderModel orderModel) {
        List<PromotionResultModel> promotionResults = this.findAllByOrder(orderModel);
        if (CollectionUtils.isEmpty(promotionResults)) {
            return new HashSet<>();
        }

        return getPromotionResultModel(promotionResults);
    }

    @Override
    public Set<PromotionSourceRuleModel> findAllPromotionSourceRulesAppliedToOrder(AbstractOrderModel orderModel) {
        Set<PromotionSourceRuleModel> sourceRuleModels = this.findAllPromotionSourceRulesByOrder(orderModel);
        Set<PromotionSourceRuleModel> promotionSourceRuleModels = new HashSet<>();
        if (CollectionUtils.isNotEmpty(sourceRuleModels)) {
            Map<String, RuleActionDefinitionData> actionMap = ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
            Map<String, RuleConditionDefinitionData> conditionMap = ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
            for (PromotionSourceRuleModel sourceRuleModel : sourceRuleModels) {
                List<RuleActionData> ruleActionData = ruleActionsService.convertActionsFromString(sourceRuleModel.getActions(), actionMap);
                List<RuleConditionData> ruleConditionData = ruleConditionsService.convertConditionsFromString(sourceRuleModel.getConditions(), conditionMap);
                if (isPromotionOnOrder(ruleActionData, ruleConditionData)) {
                    promotionSourceRuleModels.add(sourceRuleModel);
                }
            }
        }
        return promotionSourceRuleModels;
    }

    private Set<PromotionSourceRuleModel> getPromotionResultModel(List<PromotionResultModel> promotionResults) {
        Set<PromotionSourceRuleModel> sourceRuleModels = new HashSet();
        for (PromotionResultModel model : promotionResults) {
            AbstractPromotionModel promotion = model.getPromotion();
            if (promotion instanceof RuleBasedPromotionModel) {
                RuleBasedPromotionModel ruleBasedPromotion = (RuleBasedPromotionModel) promotion;
                DroolsRuleModel droolsRuleModel = ruleBasedPromotion.getRule();
                if (droolsRuleModel != null && droolsRuleModel.getPromotionSourceRule() != null) {
                    sourceRuleModels.add(droolsRuleModel.getPromotionSourceRule());
                }
            }
        }
        return sourceRuleModels;
    }

    protected boolean isPromotionOnOrder(List<RuleActionData> ruleActionData, List<RuleConditionData> ruleConditionData) {
        Optional<RuleActionData> firstConditionOption = ruleActionData.stream()
                .filter(c -> PromotionDefinitionCode.ORDER_FIXED_DISCOUNT_ACTION.code().equals(c.getDefinitionId())
                        || PromotionDefinitionCode.ORDER_PERCENTAGE_DISCOUNT_ACTION.code().equals(c.getDefinitionId()))
                .findFirst();
        if(firstConditionOption.isPresent()) {
            return true;
        }

        Optional<RuleConditionData> orderTotalCondition = ruleConditionData.stream()
                .filter(c -> PromotionDefinitionCode.ORDER_TOTAL.code().equals(c.getDefinitionId()))
                .findFirst();

        return orderTotalCondition.isPresent();
    }


    @Override
    public Set<PromotionSourceRuleModel> findAllPromotionSourceRulesAppliedToOrderEntry(AbstractOrderEntryModel orderEntryModel) {
        List<PromotionResultModel> promotionResults = promotionResultRepository.findAllByOrderEntryId(orderEntryModel.getId());
        return getPromotionResultModel(promotionResults);
    }

    @Override
    public Long getTotalAppliedQuantityOf(AbstractOrderEntryModel orderEntryModel) {
        return promotionOrderEntryConsumedRepository.sumQuantityByOrderEntry(orderEntryModel.getId());
    }

    @Autowired
    public void setPromotionOrderEntryConsumedRepository(PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository) {
        this.promotionOrderEntryConsumedRepository = promotionOrderEntryConsumedRepository;
    }

    @Autowired
    public void setRuleActionsRegistry(RuleActionsRegistry ruleActionsRegistry) {
        this.ruleActionsRegistry = ruleActionsRegistry;
    }

    @Autowired
    public void setRuleActionsService(RuleActionsService ruleActionsService) {
        this.ruleActionsService = ruleActionsService;
    }

    @Autowired
    public void setRuleConditionsService(RuleConditionsService ruleConditionsService) {
        this.ruleConditionsService = ruleConditionsService;
    }

    @Autowired
    public void setRuleConditionsRegistry(RuleConditionsRegistry ruleConditionsRegistry) {
        this.ruleConditionsRegistry = ruleConditionsRegistry;
    }
}
