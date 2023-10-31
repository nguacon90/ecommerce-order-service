package com.vctek.orderservice.converter.populator;

import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.orderservice.converter.promotion.PromotionSourceRuleActionDataPopulator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.facade.RuleActionDefinitionFacade;
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

public class PromotionSourceRuleActionDataPopulatorTest {
    private PromotionSourceRuleActionDataPopulator populator;

    @Mock
    private RuleActionDefinitionFacade ruleActionDefinitionFacade;
    @Mock
    private RuleParameterValueConverter ruleParameterValueConverter;
    private RuleActionData data = new RuleActionData();
    private ActionDTO dto = new ActionDTO();
    private Map<String, ParameterDTO> params = new HashMap<>();
    private ParameterDTO valueDto = new ParameterDTO();
    @Mock
    private RuleActionDefinitionData definitionData;
    private RuleParameterDefinitionData value = new RuleParameterDefinitionData();
    private Map<String, RuleParameterDefinitionData> definitionParams = new HashMap<>();
    private Map<String, BigDecimal> mapValue = new HashMap<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new PromotionSourceRuleActionDataPopulator(ruleActionDefinitionFacade, ruleParameterValueConverter);
        value.setType("Map(ItemType(Currency), java.math.BigDecimal)");
        definitionParams.put("value", value);
    }

    @Test
    public void populate() {
        when(definitionData.getParameters()).thenReturn(definitionParams);
        when(ruleActionDefinitionFacade.findByDefinitionId(anyString())).thenReturn(definitionData);
        dto.setDefinitionId("definitionId");
        dto.setParameters(params);
        mapValue.put("VND", new BigDecimal(200));
        valueDto.setValue(mapValue);
        params.put("value", valueDto);

        populator.populate(dto, data);
        assertEquals("definitionId", data.getDefinitionId());
    }
}
