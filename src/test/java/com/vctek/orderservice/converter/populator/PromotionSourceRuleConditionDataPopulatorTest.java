package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.orderservice.converter.promotion.PromotionSourceRuleConditionDataPopulator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.facade.RuleConditionDefinitionFacade;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class PromotionSourceRuleConditionDataPopulatorTest {
    private PromotionSourceRuleConditionDataPopulator populator;

    @Mock
    private RuleConditionDefinitionFacade ruleConditionDefinitionFacade;
    @Mock
    private RuleParameterValueConverter ruleParameterValueConverter;
    @Mock
    private RuleConditionDefinitionData definitionData;

    private RuleConditionData data = new RuleConditionData();
    private ConditionDTO dto = new ConditionDTO();
    private Map<String, ParameterDTO> params = new HashMap<>();
    private ParameterDTO valueDto = new ParameterDTO();
    private RuleParameterDefinitionData value = new RuleParameterDefinitionData();
    private Map<String, RuleParameterDefinitionData> definitionParams = new HashMap<>();
    private Map<String, BigDecimal> mapValue = new HashMap<>();
    private ParameterDTO operatorDto = new ParameterDTO();
    private RuleParameterDefinitionData operator = new RuleParameterDefinitionData();
    private JavaType javaType = new ObjectMapper().getTypeFactory().constructType(Enum.class);


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new PromotionSourceRuleConditionDataPopulator(ruleConditionDefinitionFacade, ruleParameterValueConverter);
        value.setType("Map(ItemType(Currency), java.math.BigDecimal)");
        operator.setType("Enum(com.vctek.orderservice.promotionengine.ruledefinition.enums.MembershipOperator)");
        definitionParams.put("value", value);
        definitionParams.put("operator", operator);
    }

    @Test
    public void populate() {
        when(ruleParameterValueConverter.fromString(anyString())).thenReturn(javaType);
        mapValue.put("VND", new BigDecimal(200));
        valueDto.setValue(mapValue);
        operatorDto.setValue("IN");
        params.put("value", valueDto);
        params.put("operator", operatorDto);
        when(definitionData.getParameters()).thenReturn(definitionParams);
        when(ruleConditionDefinitionFacade.findByDefinitionId(anyString())).thenReturn(definitionData);
        dto.setDefinitionId("definitionId");
        dto.setParameters(params);

        populator.populate(dto, data);
        assertEquals("definitionId", data.getDefinitionId());
    }
}
