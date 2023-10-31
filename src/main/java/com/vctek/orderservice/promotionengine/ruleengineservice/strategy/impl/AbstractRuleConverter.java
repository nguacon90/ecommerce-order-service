package com.vctek.orderservice.promotionengine.ruleengineservice.strategy.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterUuidGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AbstractRuleConverter {
    protected static final String RULE_PARAMETER_VALUE_CONVERTER_KEY = "ruleParameterValueConverter";
    protected RuleParameterValueConverter ruleParameterValueConverter;
    protected RuleParameterUuidGenerator ruleParameterUuidGenerator;
    private boolean debugMode = false;
    private ObjectReader objectReader;
    private ObjectWriter objectWriter;

    public AbstractRuleConverter(RuleParameterValueConverter ruleParameterValueConverter,
                                 RuleParameterUuidGenerator ruleParameterUuidGenerator) {
        this.ruleParameterValueConverter = ruleParameterValueConverter;
        this.ruleParameterUuidGenerator = ruleParameterUuidGenerator;
    }


    protected ObjectReader getObjectReader() {
        return this.objectReader;
    }

    protected ObjectWriter getObjectWriter() {
        return this.objectWriter;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, this.debugMode);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.addMixIn(RuleParameterData.class, RuleParameterDataMixIn.class);
        Map<Object, Object> attributes = new HashMap();
        attributes.put(RULE_PARAMETER_VALUE_CONVERTER_KEY, this.ruleParameterValueConverter);
        this.objectReader = objectMapper.reader().withAttributes(attributes);
        this.objectWriter = objectMapper.writer().withAttributes(attributes);
    }

    protected static class RuleParameterDataMixIn {
        @JsonSerialize(
                using = RuleParameterValueSerializer.class
        )
        @JsonDeserialize(
                using = RuleParameterValueDeserializer.class
        )
        public Object value;

        protected RuleParameterDataMixIn() {
        }
    }

    protected static class RuleParameterValueDeserializer extends JsonDeserializer<Object> {
        protected RuleParameterValueDeserializer() {
        }

        public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            TreeNode tree = parser.getCodec().readTree(parser);
            return tree.toString();
        }
    }

    protected static class RuleParameterValueSerializer extends JsonSerializer<Object> {
        protected RuleParameterValueSerializer() {
        }

        public void serialize(Object value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            try {
                RuleParameterValueConverter ruleParameterValueConverter = (RuleParameterValueConverter)provider.getAttribute(RULE_PARAMETER_VALUE_CONVERTER_KEY);
                String parameterValue = ruleParameterValueConverter.toString(value);
                generator.writeRawValue(parameterValue);
            } catch (RuleConverterException var6) {
                throw new JsonGenerationException(var6.getMessage(), generator);
            }
        }
    }

    protected void convertParameters(AbstractRuleData condition, AbstractRuleDefinitionData abstractRuleDefinitionData) {
        Iterator var4 = abstractRuleDefinitionData.getParameters().entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, RuleParameterDefinitionData> entry = (Map.Entry)var4.next();
            String parameterId = entry.getKey();
            RuleParameterDefinitionData parameterDefinition = entry.getValue();
            RuleParameterData parameter = condition.getParameters().get(parameterId);
            if (parameter == null) {
                parameter = new RuleParameterData();
                parameter.setValue(parameterDefinition.getDefaultValue());
                condition.getParameters().put(parameterId, parameter);
            } else {
                Object value = this.ruleParameterValueConverter.fromString((String)parameter.getValue(), parameterDefinition.getType());
                parameter.setValue(value);
            }

            parameter.setType(parameterDefinition.getType());
            if (StringUtils.isBlank(parameter.getUuid())) {
                parameter.setUuid(this.ruleParameterUuidGenerator.generateUuid(parameter, parameterDefinition));
            }
        }
    }
}
