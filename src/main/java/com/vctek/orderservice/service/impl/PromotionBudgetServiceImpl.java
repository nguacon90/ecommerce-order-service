package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionBudgetModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.PromotionBudgetRAO;
import com.vctek.orderservice.repository.PromotionBudgetRepository;
import com.vctek.orderservice.service.PromotionBudgetService;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PromotionBudgetServiceImpl implements PromotionBudgetService {

    private PromotionBudgetRepository promotionBudgetRepository;

    public PromotionBudgetServiceImpl(PromotionBudgetRepository promotionBudgetRepository) {
        this.promotionBudgetRepository = promotionBudgetRepository;
    }

    @Override
    @Transactional
    public void createPromotionBudget(PromotionSourceRuleModel sourceRuleModel, PromotionSourceRuleData promotionSourceRuleData) {
        List<RuleConditionData> conditions = promotionSourceRuleData.getConditions();
        RuleConditionData ruleBudgetConditionData = conditions.stream().filter(c -> c.getDefinitionId().equalsIgnoreCase(PromotionDefinitionCode.VCTEK_BUDGET_PER_CUSTOMER.code()))
                .findFirst().orElse(null);
        List<PromotionBudgetModel> budgetModels = promotionBudgetRepository.findAllByPromotionSourceRuleModel(sourceRuleModel);
        if(ruleBudgetConditionData == null) {
            if(CollectionUtils.isNotEmpty(budgetModels)) {
                promotionBudgetRepository.deleteAll(budgetModels);
            }
            return;
        }

        List<RuleConditionData> conditionData = conditions.stream()
                .filter(c -> c.getDefinitionId().equalsIgnoreCase(PromotionDefinitionCode.VCTEK_TARGET_CUSTOMERS.code()))
                .collect(Collectors.toList());
        Set<Long> customerGroups = new HashSet<>();
        conditionData.stream().forEach(c -> {
            List customerGroupIds = (List) c.getParameters().get(ConditionDefinitionParameter.CUSTOMER_GROUPS.code()).getValue();
            customerGroupIds.forEach(cg -> customerGroups.add(Long.valueOf(cg.toString())));
        });
        Double budgetAmount = Double.valueOf(ruleBudgetConditionData.getParameters().get(ConditionDefinitionParameter.VALUE.code()).getValue().toString());
        String scheduleType = (String) ruleBudgetConditionData.getParameters().get(ConditionDefinitionParameter.SCHEDULE.code()).getValue();
        List<PromotionBudgetModel> updatedModels = new ArrayList<>();
        for(Long customerGroupId : customerGroups) {
            PromotionBudgetModel model = getOrCreateBy(customerGroupId, budgetModels);
            model.setBudgetAmount(budgetAmount);
            model.setPromotionSourceRuleModel(sourceRuleModel);
            model.setCustomerGroupId(customerGroupId);
            model.setScheduleType(scheduleType);
            updatedModels.add(model);
        }
        if(CollectionUtils.isNotEmpty(budgetModels)) {
            promotionBudgetRepository.deleteAll(budgetModels);
        }
        promotionBudgetRepository.saveAll(updatedModels);
    }

    @Override
    public List<PromotionBudgetModel> findAllBy(PromotionSourceRuleModel rule) {
        return promotionBudgetRepository.findAllByPromotionSourceRuleModel(rule);
    }

    @Override
    public List<PromotionBudgetRAO> findAllOf(List<Long> userGroupIds) {
        List<PromotionBudgetRAO> result = new ArrayList<>();
        List<PromotionBudgetModel> promotionBudgetModels = promotionBudgetRepository.findAllByCustomerGroupIdIn(userGroupIds);
        for(PromotionBudgetModel model : promotionBudgetModels) {
            PromotionBudgetRAO promotionBudgetRAO = new PromotionBudgetRAO();
            promotionBudgetRAO.setRemainDiscount(model.getBudgetAmount());
            promotionBudgetRAO.setSourceRuleId(model.getPromotionSourceRuleModel().getId());
            if(!result.contains(promotionBudgetRAO)) {
                result.add(promotionBudgetRAO);
            }
        }
        return result;
    }

    private PromotionBudgetModel getOrCreateBy(Long customerGroupId, List<PromotionBudgetModel> budgetModels) {
        PromotionBudgetModel model = budgetModels.stream().filter(b -> b.getCustomerGroupId().equals(customerGroupId))
                .findFirst().orElse(null);
        if(model != null) {
            budgetModels.remove(model);
        }
        return model == null ? new PromotionBudgetModel() : model;
    }
}
