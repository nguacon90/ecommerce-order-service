package com.vctek.orderservice.promotionengine.ruleengineservice.strategy.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterUuidGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParametersConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Component
public class DefaultRuleParametersConverter extends AbstractRuleConverter implements RuleParametersConverter {

    public DefaultRuleParametersConverter(RuleParameterValueConverter ruleParameterValueConverter,
                                          RuleParameterUuidGenerator ruleParameterUuidGenerator) {
        super(ruleParameterValueConverter, ruleParameterUuidGenerator);
    }

    public String toString(List<RuleParameterData> parameters) {
        try {
            return this.getObjectWriter().writeValueAsString(parameters);
        } catch (IOException var3) {
            throw new RuleConverterException(var3);
        }
    }

    public List<RuleParameterData> fromString(String parameters) {
        if (StringUtils.isBlank(parameters)) {
            return Collections.emptyList();
        } else {
            try {
                ObjectReader objectReader = this.getObjectReader();
                JavaType javaType = objectReader.getTypeFactory().constructCollectionType(List.class, RuleParameterData.class);
                List<RuleParameterData> parsedParameters = objectReader.forType(javaType).readValue(parameters);
                this.convertParameterValues(parsedParameters);
                return parsedParameters;
            } catch (IOException var5) {
                throw new RuleConverterException(var5);
            }
        }
    }

    protected void convertParameterValues(List<RuleParameterData> parameters) {
        if (!CollectionUtils.isEmpty(parameters)) {
            Iterator var3 = parameters.iterator();

            while(var3.hasNext()) {
                RuleParameterData parameter = (RuleParameterData)var3.next();
                Object value = ruleParameterValueConverter.fromString((String)parameter.getValue(), parameter.getType());
                parameter.setValue(value);
            }

        }
    }
}