package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.facade.RuleConditionDefinitionFacade;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class PromotionSourceRuleConditionDataPopulator extends AbstractPromotionSourceRuleParameterPopulator
        implements Populator<ConditionDTO, RuleConditionData> {
    private RuleConditionDefinitionFacade ruleConditionDefinitionFacade;

    public PromotionSourceRuleConditionDataPopulator(RuleConditionDefinitionFacade ruleConditionDefinitionFacade,
                                                     RuleParameterValueConverter ruleParameterValueConverter) {
        super(ruleParameterValueConverter);
        this.ruleConditionDefinitionFacade = ruleConditionDefinitionFacade;
    }

    @Override
    public void populate(ConditionDTO source, RuleConditionData target) {
        RuleConditionDefinitionData definitionData = populateRuleConditionData(source, target);
        if(Boolean.TRUE.equals(definitionData.getAllowsChildren())
                && CollectionUtils.isNotEmpty(source.getChildren())) {
            RuleConditionData child;
            List<RuleConditionData> children = new ArrayList<>();
            for(ConditionDTO conditionDTO : source.getChildren()) {
                child = new RuleConditionData();
                populateRuleConditionData(conditionDTO, child);
                children.add(child);
            }

            target.setChildren(children);
        }

    }

    private RuleConditionDefinitionData populateRuleConditionData(ConditionDTO conditionDTO, RuleConditionData ruleConditionData) {
        String definitionId = conditionDTO.getDefinitionId();
        if (StringUtils.isBlank(definitionId)) {
            ErrorCodes err = ErrorCodes.INVALID_DEFINITION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{definitionId});
        }

        RuleConditionDefinitionData definitionData = ruleConditionDefinitionFacade.findByDefinitionId(definitionId);
        if (definitionData == null) {
            ErrorCodes err = ErrorCodes.INVALID_DEFINITION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{definitionId});
        }

        ruleConditionData.setDefinitionId(definitionId);
        Map<String, ParameterDTO> parameters = conditionDTO.getParameters();
        Map<String, RuleParameterDefinitionData> ruleParameterDefinitions = definitionData.getParameters();
        Map<String, RuleParameterData> ruleParameterDataMap = convertParametersToRuleParameters(definitionId, parameters, ruleParameterDefinitions);
        ruleConditionData.setParameters(ruleParameterDataMap);
        if(Boolean.TRUE.equals(definitionData.getAllowsChildren())
                && CollectionUtils.isNotEmpty(conditionDTO.getChildren())) {
            Iterator<ConditionDTO> iterator = conditionDTO.getChildren().iterator();
            while (iterator.hasNext()) {
                RuleConditionData subChild = new RuleConditionData();
                this.populateRuleConditionData(iterator.next(), subChild);
                if(CollectionUtils.isEmpty(ruleConditionData.getChildren())) {
                    ruleConditionData.setChildren(new ArrayList<>());
                }

                ruleConditionData.getChildren().add(subChild);
            }
        }
        return definitionData;
    }

}
