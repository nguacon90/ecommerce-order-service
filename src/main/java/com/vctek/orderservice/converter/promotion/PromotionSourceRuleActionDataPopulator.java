package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.facade.RuleActionDefinitionFacade;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromotionSourceRuleActionDataPopulator extends AbstractPromotionSourceRuleParameterPopulator
        implements Populator<ActionDTO, RuleActionData> {

    private RuleActionDefinitionFacade ruleActionDefinitionFacade;

    public PromotionSourceRuleActionDataPopulator(RuleActionDefinitionFacade ruleActionDefinitionFacade,
                                                  RuleParameterValueConverter ruleParameterValueConverter) {
        super(ruleParameterValueConverter);
        this.ruleActionDefinitionFacade = ruleActionDefinitionFacade;
    }

    @Override
    public void populate(ActionDTO source, RuleActionData target) {
        String definitionId = source.getDefinitionId();
        if (StringUtils.isBlank(definitionId)) {
            ErrorCodes err = ErrorCodes.INVALID_DEFINITION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{definitionId});
        }

        RuleActionDefinitionData definitionData = ruleActionDefinitionFacade.findByDefinitionId(definitionId);
        if (definitionData == null) {
            ErrorCodes err = ErrorCodes.INVALID_DEFINITION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{definitionId});
        }

        target.setDefinitionId(definitionId);

        Map<String, ParameterDTO> parameters = source.getParameters();
        Map<String, RuleParameterDefinitionData> ruleParameterDefinitions = definitionData.getParameters();
        Map<String, RuleParameterData> ruleParameterDataMap = convertParametersToRuleParameters(definitionId, parameters, ruleParameterDefinitions);
        target.setParameters(ruleParameterDataMap);
    }

}
