package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.util.Map2StringUtils;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.AbstractRuleDefinitionParameterModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractDefinitionPopulator {

    private RuleParameterValueConverter ruleParameterValueConverter;

    protected AbstractDefinitionPopulator(RuleParameterValueConverter ruleParameterValueConverter) {
        this.ruleParameterValueConverter = ruleParameterValueConverter;
    }

    protected Map<String, String> translatorParameters(String parameterMap) {
        return Map2StringUtils.stringToMap(parameterMap);
    }

    protected void populateParameters(AbstractRuleDefinitionParameterModel source, RuleParameterDefinitionData target) {
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setPriority(source.getPriority());
        target.setType(source.getType());
        target.setRequired(source.isRequired());
        String validators = source.getValidators();
        if(StringUtils.isNotBlank(validators)) {
            target.setValidators(Arrays.asList(validators.split(CommonUtils.COMMA)));
        } else {
            target.setValidators(Collections.emptyList());
        }

        Object defaultValue = this.ruleParameterValueConverter.fromString(source.getValue(), source.getType());
        target.setDefaultValue(defaultValue);
    }
}
