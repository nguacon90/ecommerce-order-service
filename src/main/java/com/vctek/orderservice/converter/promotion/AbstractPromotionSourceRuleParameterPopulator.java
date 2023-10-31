package com.vctek.orderservice.converter.promotion;

import com.fasterxml.jackson.databind.JavaType;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractPromotionSourceRuleParameterPopulator {
    private RuleParameterValueConverter ruleParameterValueConverter;

    protected AbstractPromotionSourceRuleParameterPopulator(RuleParameterValueConverter ruleParameterValueConverter) {
        this.ruleParameterValueConverter = ruleParameterValueConverter;
    }

    protected Map<String, RuleParameterData> convertParametersToRuleParameters(String definitionId, Map<String, ParameterDTO> parameters,
                                                                               Map<String, RuleParameterDefinitionData> ruleParameterDefinitions) {
        Map<String, RuleParameterData> ruleParameters = new HashMap();
        if (MapUtils.isEmpty(parameters)) {
            return ruleParameters;
        }

        String parameterCode;
        RuleParameterData ruleParameter;
        for (Iterator var5 = ruleParameterDefinitions.entrySet().iterator(); var5.hasNext();
             ruleParameters.put(parameterCode, ruleParameter)) {
            Map.Entry<String, RuleParameterDefinitionData> entry = (Map.Entry) var5.next();
            parameterCode = entry.getKey();
            RuleParameterDefinitionData ruleParameterDefinition = entry.getValue();
            ParameterDTO parameter = parameters.get(parameterCode);
            if (parameter == null && Boolean.TRUE.equals(ruleParameterDefinition.getRequired())) {
                ErrorCodes err = ErrorCodes.PARSE_PARAMETERS_ERROR;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{definitionId});
            }

            if(parameter == null) {
                parameter = new ParameterDTO();
                parameter.setType(ruleParameterDefinition.getType());
                parameter.setValue(ruleParameterDefinition.getDefaultValue());
            }

            Object value = parameter.getValue();
            String definitionType = ruleParameterDefinition.getType();

            if ("operator".equalsIgnoreCase(parameterCode)) {
                validate(value, definitionType, definitionId);
            }

            ruleParameter = new RuleParameterData();
            String uuid = StringUtils.isNotBlank(parameter.getUuid()) ? parameter.getUuid() : UUID.randomUUID().toString();
            ruleParameter.setUuid(uuid);
            ruleParameter.setType(definitionType);
            ruleParameter.setValue(value);
        }
        return ruleParameters;
    }

    protected void validate(Object operatorValue, String type, String definitionId) {
        if (operatorValue == null) {
            return;
        }
        try {
            JavaType javaType = ruleParameterValueConverter.fromString(type);
            if (javaType.isEnumType()) {
                String value = "\"" + operatorValue.toString() + "\"";
                ruleParameterValueConverter.fromString(value, type);
            }
        } catch (RuleConverterException e) {
            ErrorCodes err = ErrorCodes.PARSE_OPERATOR_ERROR;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{definitionId});
        }
    }

}
