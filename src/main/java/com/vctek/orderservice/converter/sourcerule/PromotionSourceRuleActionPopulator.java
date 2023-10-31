package com.vctek.orderservice.converter.sourcerule;

import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleActionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("promotionSourceRuleActionPopulator")
public class PromotionSourceRuleActionPopulator extends AbstractPromotionSourceRulePopulator
        implements Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> {

    private RuleActionsConverter ruleActionsConverter;
    private RuleActionsRegistry ruleActionsRegistry;

    @Override
    public void populate(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        List<ActionDTO> actions = new ArrayList<>();
        Map<String, RuleActionDefinitionData> actionDefinitionData = ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        List<RuleActionData> ruleActionData = ruleActionsConverter.fromString(source.getActions(), actionDefinitionData);

        for(RuleActionData actionData : ruleActionData) {
            ActionDTO dto = new ActionDTO();
            String definitionId = actionData.getDefinitionId();
            dto.setDefinitionId(definitionId);
            dto.setParameters(populateParameters(actionData.getParameters()));
            actions.add(dto);
        }

        target.setActions(actions);
    }

    @Autowired
    public void setRuleActionsConverter(RuleActionsConverter ruleActionsConverter) {
        this.ruleActionsConverter = ruleActionsConverter;
    }

    @Autowired
    public void setRuleActionsRegistry(RuleActionsRegistry ruleActionsRegistry) {
        this.ruleActionsRegistry = ruleActionsRegistry;
    }
}
