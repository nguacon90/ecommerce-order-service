package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.exception.DroolsRuleValueFormatterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter.DroolsRuleValueFormatter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Component
public class DefaultDroolsRuleValueFormatter implements DroolsRuleValueFormatter {

    public static final String NULL_VALUE = "null";
    private final Map<String, DroolsRuleValueFormatterHelper> formatters = new HashMap();

    @PostConstruct
    public void initFormatters() {
        this.formatters.put(Boolean.class.getName(), (context, value) -> (Boolean)value ? "Boolean.TRUE" : "Boolean.FALSE");
        this.formatters.put(Character.class.getName(), (context, value) -> "'" + value + "'");
        this.formatters.put(String.class.getName(), (context, value) -> "\"" + value + "\"");
        this.formatters.put(Byte.class.getName(), (context, value) -> "new Byte(" + value + ")");
        this.formatters.put(Short.class.getName(), (context, value) -> "new Short(" + value + ")");
        this.formatters.put(Integer.class.getName(), (context, value) -> "new Integer(" + value + ")");
        this.formatters.put(Long.class.getName(), (context, value) -> "new Long(" + value + ")");
        this.formatters.put(Float.class.getName(), (context, value) -> "new Float(" + value + ")");
        this.formatters.put(Double.class.getName(), (context, value) -> "new Double(" + value + ")");
        this.formatters.put(BigInteger.class.getName(), (context, value) -> "new " + context.generateClassName(BigInteger.class) + "(" + value + ")");
        this.formatters.put(BigDecimal.class.getName(), (context, value) -> "new " + context.generateClassName(BigDecimal.class) + "(\"" + value + "\")");
        this.formatters.put(Enum.class.getName(), (context, value) -> value.getClass().getName() + "." + ((Enum)value).name());
        this.formatters.put(Date.class.getName(), (context, value) -> "new " + context.generateClassName(Date.class) + "(" + ((Date)value).getTime() + ")");
        this.formatters.put(AbstractList.class.getName(), (context, value) -> {
            StringJoiner joiner = new StringJoiner(", ", "(", ")");
            ((List)value).stream().forEach((v) -> joiner.add(this.formatValue(context, v)));
            return joiner.toString();
        });
        this.formatters.put(AbstractMap.class.getName(), (context, value) -> {
            StringJoiner joiner = new StringJoiner(", ", "[", "]");
            Map<Object, Object> valueMap = (Map) value;
            valueMap.entrySet().stream().forEach((e) -> joiner.add(this.formatValue(context, e.getKey()) + ":"
                    + this.formatValue(context, e.getValue())));
            return joiner.toString();
        });
    }

    protected Map<String, DroolsRuleValueFormatterHelper> getFormatters() {
        return this.formatters;
    }

    public String formatValue(DroolsRuleGeneratorContext context, Object value) {
        if (this.isNullValue(value)) {
            return NULL_VALUE;
        } else {
            Class valueClass = value.getClass();

            do {
                DroolsRuleValueFormatterHelper formatter = this.formatters.get(valueClass.getName());
                if (formatter != null) {
                    return formatter.format(context, value);
                }

                valueClass = valueClass.getSuperclass();
            } while(Objects.nonNull(valueClass));

            throw new DroolsRuleValueFormatterException("Cannot find the value formatter for an object of type: " + value.getClass().getName());
        }
    }

    protected boolean isNullValue(Object value) {
        if (value instanceof Collection) {
            return CollectionUtils.isEmpty((Collection)value);
        } else {
            return value instanceof Map ? MapUtils.isEmpty((Map)value) : Objects.isNull(value);
        }
    }

    @FunctionalInterface
    protected interface DroolsRuleValueFormatterHelper<V> {
        String format(DroolsRuleGeneratorContext var1, V var2);
    }
}
