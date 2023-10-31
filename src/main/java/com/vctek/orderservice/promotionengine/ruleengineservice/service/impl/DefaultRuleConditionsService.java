package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.dto.PromotionResultData;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.AmountOperator;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleConditionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DefaultRuleConditionsService extends AbstractRuleService implements RuleConditionsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleConditionsService.class);
    private RuleConditionsConverter ruleConditionsConverter;
    private RuleConditionsRegistry ruleConditionsRegistry;

    public DefaultRuleConditionsService(RuleParametersService ruleParametersService,
                                        RuleConditionsConverter ruleConditionsConverter) {
        super(ruleParametersService);
        this.ruleConditionsConverter = ruleConditionsConverter;
    }

    @Override
    public RuleConditionData createConditionFromDefinition(RuleConditionDefinitionData definition) {
        RuleConditionData condition = new RuleConditionData();
        condition.setDefinitionId(definition.getCode());
        condition.setParameters(super.populateRuleParams(definition.getParameters()));
        condition.setChildren(new ArrayList());
        return condition;
    }

    @Override
    public String convertConditionsToString(List<RuleConditionData> conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions) {
        return this.ruleConditionsConverter.toString(conditions, conditionDefinitions);
    }

    @Override
    public List<RuleConditionData> convertConditionsFromString(String conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions) {
        return this.ruleConditionsConverter.fromString(conditions, conditionDefinitions);
    }

    @Override
    public List<PromotionResultData> sortSourceRulesByOrderTotalCondition(Set<PromotionSourceRuleModel> promotionSourceRuleModelList) {
        List<PromotionResultData> couldFirePromotionData = new ArrayList<>();
        if(CollectionUtils.isEmpty(promotionSourceRuleModelList)) {
            return couldFirePromotionData;
        }

        Map<String, RuleConditionDefinitionData> conditionDefinitions = this.ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        PromotionResultData data;
        for (PromotionSourceRuleModel sourceRuleModel : promotionSourceRuleModelList) {
            data = new PromotionResultData();
            double minOrderValue = getMinOrderTotalValueCondition(conditionDefinitions, sourceRuleModel);
            data.setMinValue(minOrderValue);
            data.setPromotionId(sourceRuleModel.getId());
            data.setMessageFired(sourceRuleModel.getMessageFired());
            data.setCode(sourceRuleModel.getCode());
            couldFirePromotionData.add(data);
        }

        Collections.sort(couldFirePromotionData, (o1, o2) -> o2.getMinValue().compareTo(o1.getMinValue()));
        return couldFirePromotionData;
    }

    @Override
    public double getMinOrderTotalValueCondition(Map<String, RuleConditionDefinitionData> conditionDefinitions, PromotionSourceRuleModel sourceRuleModel) {
        try {
            List<RuleConditionData> ruleConditionData = this.convertConditionsFromString(sourceRuleModel.getConditions(), conditionDefinitions);
            Optional<RuleConditionData> firstConditionOption = ruleConditionData.stream()
                    .filter(c -> PromotionDefinitionCode.ORDER_TOTAL.code().equals(c.getDefinitionId())
                            && c.getParameters().get(ConditionDefinitionParameter.OPERATOR.code()) != null)
                    .filter(c -> {
                        RuleParameterData ruleParameterData = c.getParameters().get(ConditionDefinitionParameter.OPERATOR.code());
                        return AmountOperator.GREATER_THAN.equals(ruleParameterData.getValue()) ||
                                AmountOperator.GREATER_THAN_OR_EQUAL.equals(ruleParameterData.getValue());
                    }).findFirst();
            if (!firstConditionOption.isPresent()) {
                return 0;
            }

            RuleConditionData conditionData = firstConditionOption.get();
            Map<String, BigDecimal> value = (Map<String, BigDecimal>) conditionData.getParameters()
                    .get(ConditionDefinitionParameter.VALUE.code()).getValue();
            BigDecimal bigDecimal = value.get(CommonUtils.CASH);
            return bigDecimal.doubleValue();
        } catch (RuntimeException e) {
            LOGGER.error("Convert condition error: ", e);
        }

        return 0;
    }

    @Autowired
    public void setRuleConditionsRegistry(RuleConditionsRegistry ruleConditionsRegistry) {
        this.ruleConditionsRegistry = ruleConditionsRegistry;
    }
}
