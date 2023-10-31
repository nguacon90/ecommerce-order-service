package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractPromotionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedPromotionModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionOrderEntryConsumedRepository;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionResultRepository;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DefaultPromotionResultServiceTest {
    @Mock
    private PromotionResultRepository promotionResultRepository;

    private DefaultPromotionResultService promotionResultService;
    @Mock
    private AbstractOrderModel orderMock;
    @Mock
    private PromotionResultModel promotionResultMock;
    @Mock
    private PromotionResultModel promotionResultMock2;
    @Mock
    private RuleBasedPromotionModel ruleBasePromotionMock;
    @Mock
    private AbstractPromotionModel abstractPromotionMock;
    @Mock
    private DroolsRuleModel droolMock;
    @Mock
    private RuleActionsRegistry ruleActionsRegistry;
    @Mock
    private RuleActionsService ruleActionsService;
    @Mock
    private PromotionSourceRuleModel sourceRuleMock;
    @Mock
    private PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository;
    @Mock
    private RuleConditionsService ruleConditionsService;
    @Mock
    private RuleConditionsRegistry ruleConditionsRegistry;
    private AbstractOrderModel model;
    private OrderEntryModel comboEntryModel;
    private OrderEntryModel normalEntryModel;
    @Mock
    private RuleActionDefinitionData partnerProductActionMock;
    @Mock
    private RuleActionDefinitionData orderFixedDiscountActionMock;

    @Mock
    private RuleActionData entryFixedDiscountAction;

    @Mock
    private RuleActionData orderFixedDiscountAction;

    private Map<String, RuleActionDefinitionData> actionMap = new HashMap<>();
    @Mock
    private PromotionSourceRuleModel entryDiscountPromotionSourceRuleMock;
    @Mock
    private PromotionSourceRuleModel orderDiscountPromotionSourceRuleMock;
    @Mock
    private RuleBasedPromotionModel ruleBasePromotionMock2;
    @Mock
    private DroolsRuleModel droolMock2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        promotionResultService = new DefaultPromotionResultService(promotionResultRepository);
        promotionResultService.setPromotionOrderEntryConsumedRepository(promotionOrderEntryConsumedRepository);
        promotionResultService.setRuleActionsRegistry(ruleActionsRegistry);
        promotionResultService.setRuleActionsService(ruleActionsService);
        promotionResultService.setRuleConditionsService(ruleConditionsService);
        promotionResultService.setRuleConditionsRegistry(ruleConditionsRegistry);
        model = new OrderModel();
        model.setId(1l);
        model.setCompanyId(2l);
        comboEntryModel = new OrderEntryModel();
        comboEntryModel.setId(1l);
        comboEntryModel.setOrder(model);
        SubOrderEntryModel subOrderEntryModel1 = new SubOrderEntryModel();
        subOrderEntryModel1.setProductId(10l);
        subOrderEntryModel1.setOrderEntry(comboEntryModel);
        comboEntryModel.setSubOrderEntries(new HashSet<>(Arrays.asList(subOrderEntryModel1)));

        normalEntryModel = new OrderEntryModel();
        normalEntryModel.setId(2l);
        normalEntryModel.setQuantity(10l);
        normalEntryModel.setFinalPrice(10000.0);
        normalEntryModel.setDiscountOrderToItem(2000.0);
        normalEntryModel.setProductId(1l);
        normalEntryModel.setOrder(model);

        model.getEntries().add(comboEntryModel);
        model.getEntries().add(normalEntryModel);
        actionMap.put(PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code(),
                partnerProductActionMock);
        actionMap.put(PromotionDefinitionCode.ORDER_FIXED_DISCOUNT_ACTION.code(), orderFixedDiscountActionMock);
    }

    @Test
    public void findAllPromotionSourceRulesByOrder_EmptySourceRule() {
        when(promotionResultRepository.findAllByOrder(orderMock)).thenReturn(new ArrayList<>());

        Set<PromotionSourceRuleModel> rules = promotionResultService.findAllPromotionSourceRulesByOrder(orderMock);
        assertEquals(0, rules.size());
    }

    @Test
    public void findAllPromotionSourceRulesByOrder() {
        when(promotionResultRepository.findAllByOrder(orderMock)).thenReturn(Arrays.asList(promotionResultMock, promotionResultMock2));
        when(promotionResultMock.getPromotion()).thenReturn(ruleBasePromotionMock);
        when(promotionResultMock2.getPromotion()).thenReturn(abstractPromotionMock);
        when(ruleBasePromotionMock.getRule()).thenReturn(droolMock);
        when(droolMock.getPromotionSourceRule()).thenReturn(sourceRuleMock);

        Set<PromotionSourceRuleModel> rules = promotionResultService.findAllPromotionSourceRulesByOrder(orderMock);
        assertEquals(1, rules.size());
    }

    @Test
    public void findAllPromotionSourceRulesAppliedToOrder() {
        when(promotionResultRepository.findAllByOrder(orderMock)).thenReturn(Arrays.asList(promotionResultMock, promotionResultMock2));
        when(promotionResultMock.getPromotion()).thenReturn(ruleBasePromotionMock);
        when(ruleBasePromotionMock.getRule()).thenReturn(droolMock);
        when(droolMock.getPromotionSourceRule()).thenReturn(entryDiscountPromotionSourceRuleMock);
        when(entryDiscountPromotionSourceRuleMock.getActions()).thenReturn("entryActions");

        when(promotionResultRepository.findAllByOrder(orderMock)).thenReturn(Arrays.asList(promotionResultMock, promotionResultMock2));
        when(promotionResultMock2.getPromotion()).thenReturn(ruleBasePromotionMock2);
        when(ruleBasePromotionMock2.getRule()).thenReturn(droolMock2);
        when(droolMock2.getPromotionSourceRule()).thenReturn(orderDiscountPromotionSourceRuleMock);
        when(orderDiscountPromotionSourceRuleMock.getActions()).thenReturn("orderActions");

        when(ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION))
                .thenReturn(actionMap);
        when(ruleActionsService.convertActionsFromString(eq("entryActions"), eq(actionMap)))
                .thenReturn(Arrays.asList(entryFixedDiscountAction));
        when(ruleActionsService.convertActionsFromString(eq("orderActions"), eq(actionMap)))
                .thenReturn(Arrays.asList(orderFixedDiscountAction));
        when(entryFixedDiscountAction.getDefinitionId()).thenReturn(PromotionDefinitionCode.ORDER_ENTRY_FIXED_DISCOUNT_ACTION.code());
        when(orderFixedDiscountAction.getDefinitionId()).thenReturn(PromotionDefinitionCode.ORDER_FIXED_DISCOUNT_ACTION.code());

        Set<PromotionSourceRuleModel> orderPromotions = promotionResultService.findAllPromotionSourceRulesAppliedToOrder(orderMock);
        assertEquals(1, orderPromotions.size());
        assertEquals(orderDiscountPromotionSourceRuleMock, orderPromotions.iterator().next());
    }
}
