package com.vctek.orderservice.promotionengine.ruleengineservice.service.impl;

import com.vctek.orderservice.dto.PromotionResultData;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.AmountOperator;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleConditionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultRuleConditionsServiceTest {
    @Mock
    private RuleParametersService ruleParametersService;
    @Mock
    private RuleConditionsConverter ruleConditionsConverter;

    private DefaultRuleConditionsService service;

    @Mock
    private RuleConditionDefinitionData data;
    private Map<String, RuleParameterDefinitionData> parameters;
    private RuleParameterData param = new RuleParameterData();
    private List<RuleConditionData> conditions = new ArrayList<>();
    private Map<String, RuleConditionDefinitionData> conditionDefinitions = new HashMap<>();

    @Mock
    private PromotionSourceRuleModel sourceRuleMock1;
    @Mock
    private PromotionSourceRuleModel sourceRuleMock2;
    @Mock
    private PromotionSourceRuleModel sourceRuleMock3;

    @Mock
    private Map<String, RuleConditionDefinitionData> conditionDefinitionMock;
    @Mock
    private RuleConditionData conditionDataMock1;
    @Mock
    private RuleConditionData conditionDataMock2;
    @Mock
    private RuleConditionData conditionDataMock3;
    @Mock
    private RuleParameterData ruleParamMock1;
    @Mock
    private RuleParameterData ruleParamMock2;
    @Mock
    private RuleParameterData ruleParamMock3;
    @Mock
    private RuleParameterData ruleValueMock1;
    @Mock
    private RuleParameterData ruleValueMock2;
    @Mock
    private RuleParameterData ruleValueMock3;
    @Mock
    private Map<String, RuleParameterData> paramMock1;
    @Mock
    private Map<String, RuleParameterData> paramMock2;
    @Mock
    private Map<String, RuleParameterData> paramMock3;
    @Mock
    private RuleConditionsRegistry ruleConditionsRegistry;

    private Map<String, Object> generateMinOrderTotal(double minOrderTotal) {
        Map<String, Object> result = new HashMap<>();
        result.put(CommonUtils.CASH, BigDecimal.valueOf(minOrderTotal));
        return result;
    }
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parameters = new HashMap<>();
        parameters.put("param1", new RuleParameterDefinitionData());
        when(ruleParametersService.createParameterFromDefinition(any(RuleParameterDefinitionData.class))).thenReturn(param);
        service = new DefaultRuleConditionsService(ruleParametersService, ruleConditionsConverter);
        service.setRuleConditionsRegistry(ruleConditionsRegistry);
    }

    @Test
    public void createConditionFromDefinition() {
        when(data.getParameters()).thenReturn(parameters);
        RuleConditionData condition = service.createConditionFromDefinition(data);
        assertNotNull(condition.getParameters());
        assertTrue(condition.getParameters().containsKey("param1"));
    }

    @Test
    public void convertConditionsToString() {
        service.convertConditionsToString(conditions, conditionDefinitions);
        verify(ruleConditionsConverter).toString(anyList(), anyMap());
    }

    @Test
    public void convertConditionsFromString() {
        service.convertConditionsFromString("condition", conditionDefinitions);
        verify(ruleConditionsConverter).fromString(anyString(), anyMap());
    }

    @Test
    public void sortCouldFirePromotions_withOrderTotalCondition() {
        when(sourceRuleMock1.getConditions()).thenReturn("condition1");
        when(sourceRuleMock2.getConditions()).thenReturn("condition2");
        when(sourceRuleMock3.getConditions()).thenReturn("condition3");
        when(ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION))
                .thenReturn(conditionDefinitionMock);
        when(ruleConditionsConverter.fromString("condition1", conditionDefinitionMock))
                .thenReturn(Arrays.asList(conditionDataMock1));
        when(ruleConditionsConverter.fromString("condition2", conditionDefinitionMock))
                .thenReturn(Arrays.asList(conditionDataMock2));
        when(ruleConditionsConverter.fromString("condition3", conditionDefinitionMock))
                .thenReturn(Arrays.asList(conditionDataMock3));
        when(conditionDataMock1.getDefinitionId()).thenReturn(PromotionDefinitionCode.ORDER_TOTAL.code());
        when(conditionDataMock2.getDefinitionId()).thenReturn(PromotionDefinitionCode.ORDER_TOTAL.code());
        when(conditionDataMock3.getDefinitionId()).thenReturn(PromotionDefinitionCode.ORDER_TOTAL.code());
        when(conditionDataMock1.getParameters()).thenReturn(paramMock1);
        when(conditionDataMock2.getParameters()).thenReturn(paramMock2);
        when(conditionDataMock3.getParameters()).thenReturn(paramMock3);
        when(paramMock1.get(ConditionDefinitionParameter.OPERATOR.code())).thenReturn(ruleParamMock1);
        when(paramMock2.get(ConditionDefinitionParameter.OPERATOR.code())).thenReturn(ruleParamMock2);
        when(paramMock3.get(ConditionDefinitionParameter.OPERATOR.code())).thenReturn(ruleParamMock3);

        when(ruleParamMock1.getValue()).thenReturn(AmountOperator.GREATER_THAN_OR_EQUAL);
        when(ruleParamMock2.getValue()).thenReturn(AmountOperator.GREATER_THAN);
        when(ruleParamMock3.getValue()).thenReturn(AmountOperator.GREATER_THAN_OR_EQUAL);

        when(paramMock1.get(ConditionDefinitionParameter.VALUE.code())).thenReturn(ruleValueMock1);
        when(paramMock2.get(ConditionDefinitionParameter.VALUE.code())).thenReturn(ruleValueMock2);
        when(paramMock3.get(ConditionDefinitionParameter.VALUE.code())).thenReturn(ruleValueMock3);

        Map<String, Object> value1 = generateMinOrderTotal(100000d);
        Map<String, Object> value2 = generateMinOrderTotal(120000d);
        Map<String, Object> value3 = generateMinOrderTotal(150000d);
        when(ruleValueMock1.getValue()).thenReturn(value1);
        when(ruleValueMock2.getValue()).thenReturn(value2);
        when(ruleValueMock3.getValue()).thenReturn(value3);

        List<PromotionResultData> promotionResultData = service.sortSourceRulesByOrderTotalCondition(new HashSet<>(Arrays.asList(sourceRuleMock1, sourceRuleMock2, sourceRuleMock3)));

        assertEquals(3, promotionResultData.size());
        assertEquals(150000d, promotionResultData.get(0).getMinValue(), 0);
        assertEquals(120000d, promotionResultData.get(1).getMinValue(), 0);
        assertEquals(100000d, promotionResultData.get(2).getMinValue(), 0);
    }
}
