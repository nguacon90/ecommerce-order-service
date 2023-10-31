package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CategoryData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class PromotionSourceRuleDataValidatorTest {
    private PromotionSourceRuleDataValidator validator;
    @Mock
    private ProductService productService;
    @Mock
    private PromotionSourceRuleData sourceRuleMock;
    @Mock
    private RuleActionData actionMock;
    private Map<String, RuleParameterData> containerMap = new HashMap<>();
    @Mock
    private RuleParameterData qualifyContainerMock;
    @Mock
    private RuleParameterData targetContainerMock;
    private Map<String, Object> qualifyValue = new HashMap<>();
    private Map<String, Object> targetValue = new HashMap<>();
    @Mock
    private RuleConditionData qualifyConditionMock;
    @Mock
    private RuleConditionData targetConditionMock;
    @Mock
    private RuleConditionData conditionGroupMock1;
    @Mock
    private RuleConditionData conditionGroupMock2;
    @Mock
    private RuleConditionData qualifyProductMock1;
    @Mock
    private RuleConditionData qualifyCategoryMock1;
    @Mock
    private RuleConditionData qualifyProductMock2;
    private Map<String, RuleParameterData> qualifyProductMap1 = new HashMap<>();
    private Map<String, RuleParameterData> qualifyProductMap2 = new HashMap<>();
    @Mock
    private RuleParameterData qualifyProductParam1;
    @Mock
    private RuleParameterData qualifyProductParam2;
    private Map<String, RuleParameterData> qualifyParamMap = new HashMap<>();
    private Map<String, RuleParameterData> targetParamMap = new HashMap<>();
    @Mock
    private RuleParameterData qualifyContainerIdMock;
    @Mock
    private RuleParameterData targetContainerIdMock;
    private Map<String, RuleParameterData> qualifyCategoryMap = new HashMap<>();
    @Mock
    private RuleParameterData qualifyCategoryParam;
    @Mock
    private CategoryData catDataMock1;
    @Mock
    private CategoryData catDataMock2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new PromotionSourceRuleDataValidator();
        validator.setProductService(productService);
        when(sourceRuleMock.getActions()).thenReturn(Arrays.asList(actionMock));
        when(sourceRuleMock.getConditions()).thenReturn(Arrays.asList(qualifyConditionMock, targetConditionMock));
        containerMap.put(ConditionDefinitionParameter.QUALIFYING_CONTAINERS.code(), qualifyContainerMock);
        containerMap.put(ConditionDefinitionParameter.TARGET_CONTAINERS.code(), targetContainerMock);
        qualifyValue.put("CONTAINER_X", 1);
        targetValue.put("CONTAINER_Y", 1);
        qualifyParamMap.put(PromotionSourceRuleDataValidator.ID_CONTAINER, qualifyContainerIdMock);
        targetParamMap.put(PromotionSourceRuleDataValidator.ID_CONTAINER, targetContainerIdMock);
        when(qualifyContainerIdMock.getValue()).thenReturn("CONTAINER_X");
        when(targetContainerIdMock.getValue()).thenReturn("CONTAINER_Y");
        when(qualifyConditionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.CONTAINER.code());
        when(targetConditionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.CONTAINER.code());
        when(qualifyConditionMock.getParameters()).thenReturn(qualifyParamMap);
        when(targetConditionMock.getParameters()).thenReturn(targetParamMap);

        when(qualifyConditionMock.getChildren()).thenReturn(Arrays.asList(conditionGroupMock1));
        when(targetConditionMock.getChildren()).thenReturn(Arrays.asList(conditionGroupMock2));
        when(conditionGroupMock1.getDefinitionId()).thenReturn(PromotionDefinitionCode.GROUP.code());
        when(conditionGroupMock1.getChildren()).thenReturn(Arrays.asList(qualifyProductMock1, qualifyCategoryMock1));
        when(conditionGroupMock2.getDefinitionId()).thenReturn(PromotionDefinitionCode.GROUP.code());
        when(conditionGroupMock2.getChildren()).thenReturn(Arrays.asList(qualifyProductMock2));

        when(qualifyProductMock1.getDefinitionId()).thenReturn(PromotionDefinitionCode.QUALIFIER_PRODUCTS.code());
        when(qualifyCategoryMock1.getDefinitionId()).thenReturn(PromotionDefinitionCode.QUALIFIER_CATEGORIES.code());
        when(qualifyProductMock2.getDefinitionId()).thenReturn(PromotionDefinitionCode.QUALIFIER_PRODUCTS.code());

        when(qualifyProductMock1.getParameters()).thenReturn(qualifyProductMap1);
        when(qualifyProductMock2.getParameters()).thenReturn(qualifyProductMap2);
        when(qualifyCategoryMock1.getParameters()).thenReturn(qualifyCategoryMap);

        qualifyProductMap1.put(ConditionDefinitionParameter.QUALIFYING_PRODUCTS.code(), qualifyProductParam1);
        qualifyProductMap2.put(ConditionDefinitionParameter.QUALIFYING_PRODUCTS.code(), qualifyProductParam2);
        qualifyCategoryMap.put(ConditionDefinitionParameter.QUALIFYING_CATEGORIES.code(), qualifyCategoryParam);

    }

    @Test
    public void validatePromotionNotProductPartner() {
        when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.ORDER_TOTAL.code());
        validator.validate(sourceRuleMock);
        assertTrue("success", true);
    }

    @Test
    public void validatePromotionProductPartner_NotFoundQualifierContainerId() {
        when(qualifyContainerMock.getValue()).thenReturn(null);
        when(targetContainerMock.getValue()).thenReturn(new HashMap<>());
        when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code());
        when(actionMock.getParameters()).thenReturn(containerMap);

        validator.validate(sourceRuleMock);
        assertTrue("success", true);
    }

    @Test
    public void validatePromotionProductPartner_NotFoundTargetContainerId() {
        when(qualifyContainerMock.getValue()).thenReturn(new HashMap<>());
        when(targetContainerMock.getValue()).thenReturn("abc");
        when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code());
        when(actionMock.getParameters()).thenReturn(containerMap);

        validator.validate(sourceRuleMock);
        assertTrue("success", true);
    }

    @Test
    public void validatePromotionProductPartner_NotFoundQualifierCondition() {
        when(qualifyContainerMock.getValue()).thenReturn(qualifyValue);
        when(targetContainerMock.getValue()).thenReturn(targetValue);
        when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code());
        when(actionMock.getParameters()).thenReturn(containerMap);
        when(qualifyConditionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.QUALIFIER_PRODUCTS.code());

        validator.validate(sourceRuleMock);
        assertTrue("success", true);
    }

    @Test
    public void validatePromotionProductPartner_OnlyOneTheSameProduct() {
        when(qualifyContainerMock.getValue()).thenReturn(qualifyValue);
        when(targetContainerMock.getValue()).thenReturn(targetValue);
        when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code());
        when(actionMock.getParameters()).thenReturn(containerMap);
        when(qualifyProductParam1.getValue()).thenReturn(Arrays.asList(1234l));
        when(qualifyProductParam2.getValue()).thenReturn(Arrays.asList(1234l));

        validator.validate(sourceRuleMock);
        assertTrue("success", true);
    }

    @Test
    public void validatePromotionProductPartner_HasTheSameProduct() {
        try {
            when(qualifyContainerMock.getValue()).thenReturn(qualifyValue);
            when(targetContainerMock.getValue()).thenReturn(targetValue);
            when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code());
            when(actionMock.getParameters()).thenReturn(containerMap);
            when(qualifyProductParam1.getValue()).thenReturn(Arrays.asList(1234l));
            when(qualifyProductParam2.getValue()).thenReturn(Arrays.asList(1234l, 222l));

            validator.validate(sourceRuleMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.DUPLICATE_PRODUCT_IN_PARTNER_PRODUCT_PROMOTION.message(), e.getMessage());
        }
    }

    @Test
    public void validatePromotionProductPartner_HasTheSameProduct_InCategory() {
        try {
            when(qualifyContainerMock.getValue()).thenReturn(qualifyValue);
            when(targetContainerMock.getValue()).thenReturn(targetValue);
            when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code());
            when(actionMock.getParameters()).thenReturn(containerMap);
            when(qualifyProductParam1.getValue()).thenReturn(Arrays.asList(1234l));
            when(qualifyCategoryParam.getValue()).thenReturn(Arrays.asList(11l, 221l));
            when(qualifyProductParam2.getValue()).thenReturn(Arrays.asList(123l, 222l));
            when(productService.findAllProductCategories(123l)).thenReturn(Arrays.asList(catDataMock1));
            when(productService.findAllProductCategories(222l)).thenReturn(Arrays.asList(catDataMock2));
            when(catDataMock1.getId()).thenReturn(111l);
            when(catDataMock2.getId()).thenReturn(11l);

            validator.validate(sourceRuleMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.DUPLICATE_PRODUCT_IN_PARTNER_PRODUCT_PROMOTION.message(), e.getMessage());
        }
    }

    @Test
    public void validatePromotionProductPartner_HasNotTheSameProduct_InCategory() {
        when(qualifyContainerMock.getValue()).thenReturn(qualifyValue);
        when(targetContainerMock.getValue()).thenReturn(targetValue);
        when(actionMock.getDefinitionId()).thenReturn(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code());
        when(actionMock.getParameters()).thenReturn(containerMap);
        when(qualifyProductParam1.getValue()).thenReturn(Arrays.asList(1234l));
        when(qualifyCategoryParam.getValue()).thenReturn(Arrays.asList(11l, 221l));
        when(qualifyProductParam2.getValue()).thenReturn(Arrays.asList(123l, 222l));
        when(productService.findAllProductCategories(123l)).thenReturn(Arrays.asList(catDataMock1));
        when(productService.findAllProductCategories(222l)).thenReturn(Arrays.asList(catDataMock2));
        when(catDataMock1.getId()).thenReturn(113l);
        when(catDataMock2.getId()).thenReturn(113l);

        validator.validate(sourceRuleMock);
        assertTrue("success", true);
    }
}
