package com.vctek.orderservice.promotionengine.ruleengineservice.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterTypeFormatter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DefaultRuleParameterTypeFormatter implements RuleParameterTypeFormatter {
    private static final String DEFAULT_PARAM_TYPE = "java.lang.String";
    private Map<String, String> formats;

    public DefaultRuleParameterTypeFormatter(@Qualifier("ruleParameterTypeFormatter") Map<String, String> formats) {
        this.formats = formats;
    }

    @Override
    public String formatParameterType(String paramType) {
        if (StringUtils.isEmpty(paramType)) {
            return DEFAULT_PARAM_TYPE;
        } else {
            String convertedType = StringUtils.EMPTY;
            if (MapUtils.isNotEmpty(this.formats)) {
                convertedType = this.formatConfigurableTypes(paramType);
            }

            if (StringUtils.isEmpty(convertedType)) {
                convertedType = paramType;
            }

            return convertedType;
        }
    }

    protected String formatConfigurableTypes(String paramType) {
        Iterator var3 = this.formats.entrySet().iterator();

        Map.Entry entry;
        Matcher typeMatcher;
        do {
            if (!var3.hasNext()) {
                return "";
            }

            entry = (Map.Entry)var3.next();
            typeMatcher = Pattern.compile((String)entry.getKey()).matcher(paramType);
        } while(!typeMatcher.matches());

        int matchesNumber = typeMatcher.groupCount();
        Object[] params = new String[matchesNumber];

        for(int i = 0; i < matchesNumber; ++i) {
            String formattedValue = this.formatParameterType(typeMatcher.group(i + 1));
            params[i] = formattedValue;
        }

        return String.format((String)entry.getValue(), params);
    }

    public Map<String, String> getFormats() {
        return this.formats;
    }

}
