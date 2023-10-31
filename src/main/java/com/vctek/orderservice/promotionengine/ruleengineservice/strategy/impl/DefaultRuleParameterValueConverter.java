package com.vctek.orderservice.promotionengine.ruleengineservice.strategy.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterTypeFormatter;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DefaultRuleParameterValueConverter implements RuleParameterValueConverter {
    protected static final Pattern ENUM_PATTERN = Pattern.compile("^Enum\\((.*)\\)");
    protected static final Pattern LIST_PATTERN = Pattern.compile("^List\\((.*)\\)");
    protected static final Pattern MAP_PATTERN = Pattern.compile("^Map\\((.+),\\s*(.+)\\)");
    private Set<String> supportedTypes;
    private RuleParameterTypeFormatter ruleParameterTypeFormatter;
    private boolean debugMode = false;
    private ObjectReader objectReader;
    private ObjectWriter objectWriter;

    public DefaultRuleParameterValueConverter(@Qualifier("supportedTypes") Set<String> supportedTypes,
                                              RuleParameterTypeFormatter ruleParameterTypeFormatter) {
        this.supportedTypes = supportedTypes;
        this.ruleParameterTypeFormatter = ruleParameterTypeFormatter;
    }

    public Set<String> getSupportedTypes() {
        return this.supportedTypes;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    protected ObjectReader getObjectReader() {
        return this.objectReader;
    }

    protected ObjectWriter getObjectWriter() {
        return this.objectWriter;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, this.debugMode);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, this.debugMode);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Map<Object, Object> attributes = new HashMap();
        this.configureObjectMapper(objectMapper);
        this.configureAttributes(attributes);
        this.objectReader = objectMapper.reader().withAttributes(attributes);
        this.objectWriter = objectMapper.writer().withAttributes(attributes);
    }

    protected void configureObjectMapper(ObjectMapper objectMapper) {
        //NOSONAR
    }

    protected void configureAttributes(Map<Object, Object> attributes) {
        //NOSONAR
    }

    @Override
    public String toString(Object value) {
        try {
            return this.getObjectWriter().writeValueAsString(value);
        } catch (IOException var3) {
            throw new RuleConverterException(var3);
        }
    }

    @Override
    public Object fromString(String value, String type) {
        if (StringUtils.isEmpty(value)) {
            return null;
        } else {
            try {
                ObjectReader objReader = this.getObjectReader();
                JavaType javaType = this.resolveJavaType(objReader.getTypeFactory(), type);
                return objReader.forType(javaType).readValue(value);
            } catch (Exception var5) {
                throw new RuleConverterException(var5);
            }
        }
    }

    @Override
    public JavaType fromString(String type) {
        ObjectReader objReader = this.getObjectReader();
        try {
            return this.resolveJavaType(objReader.getTypeFactory(), type);
        } catch (ClassNotFoundException e) {
            throw new RuleConverterException(e);
        }
    }

    protected JavaType resolveJavaType(TypeFactory typeFactory, String type) throws ClassNotFoundException {
        if (StringUtils.isEmpty(type)) {
            throw new RuleConverterException("Type cannot be null");
        }
        String valueType = this.ruleParameterTypeFormatter.formatParameterType(type);
        if (this.supportedTypes.contains(valueType)) {
            Class<?> typeClass = this.getClassForType(valueType);
            return typeFactory.constructType(typeClass);
        }
        Matcher enumMatcher = ENUM_PATTERN.matcher(valueType);
        if (enumMatcher.matches()) {
            Class<?> enumClass = this.getClassForType(enumMatcher.group(1));
            return typeFactory.constructType(enumClass);
        }
        Matcher listMatcher = LIST_PATTERN.matcher(valueType);
        if (listMatcher.matches()) {
            Class<?> elementClass = this.getClassForType(listMatcher.group(1));
            return typeFactory.constructCollectionType(List.class, elementClass);
        }
        Matcher mapMatcher = MAP_PATTERN.matcher(valueType);
        if (mapMatcher.matches()) {
            Class<?> keyClass = this.getClassForType(mapMatcher.group(1));
            Class<?> valueClass = this.getClassForType(mapMatcher.group(2));
            return typeFactory.constructMapType(Map.class, keyClass, valueClass);
        }
        throw new RuleConverterException("Type " + type + " is not supported");
    }

    protected Class<?> getClassForType(String type) throws ClassNotFoundException {
        return Class.forName(type, true, this.getClass().getClassLoader());
    }
}
