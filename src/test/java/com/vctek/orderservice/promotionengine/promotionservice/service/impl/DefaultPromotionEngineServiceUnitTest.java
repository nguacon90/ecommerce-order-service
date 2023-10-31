package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.promotionservice.strategy.impl.DefaultOrderAdjustTotalActionStrategy;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationResult;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleEngineRuntimeException;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsRuleEngineContextRepository;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import com.vctek.orderservice.promotionengine.ruleengine.service.RuleEngineService;
import com.vctek.orderservice.promotionengine.ruleengineservice.action.RuleActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.FactContextFactory;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.FactContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.FactContextType;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleActionStrategy;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.service.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DefaultPromotionEngineServiceUnitTest {
    private static final String RULE_BASED_PROMOTION_DESC = "RuleBasedPromotion description";
    private static final String NOT_RULE_BASED_PROMOTION_DESC = "Not RuleBasedPromotion description";

    @Mock
    private RuleBasedPromotionModel ruleBasedPromotion;

    @Mock
    private AbstractPromotionModel abstractPromotion;

    @Mock
    private FactContextFactory factContextFactory;

    @Mock
    private FactContext factContext;

    @Mock
    private CartModel cart;

    @Mock
    private RuleEngineService commerceRuleEngineService;

    @Mock
    private DroolsRuleEngineContextRepository droolsRuleEngineContextRepository;

    @Mock
    private CalculationService calculationService;

    @Mock
    private PromotionSourceRuleService promotionSourceRuleService;

    @Mock
    private RuleActionService ruleActionService;

    @Mock
    private ModelService modelService;

    @Mock
    private RuleEvaluationResult evaluationResult;


    @Mock
    private DiscountRAO discountRAO1;
    @Mock
    private DiscountRAO discountRAO2;
    @Mock
    private DiscountRAO discountRAO3;

    @Mock
    private PromotionSourceRuleModel promotionSourceRuleModel1;
    @Mock
    private PromotionSourceRuleModel promotionSourceRuleModel2;
    @Mock
    private PromotionSourceRuleModel promotionSourceRuleModel3;
    @Mock
    private PromotionResultModel promotionResultModel;
    @Mock
    private DefaultOrderAdjustTotalActionStrategy adjustTotalActionStrategy;
    @Mock
    private PromotionResultService promotionResultService;

    private RuleEngineResultRAO ruleEngineResultRAO = new RuleEngineResultRAO();
    private DefaultPromotionEngineService defaultPromotionEngineService;
    private List<RuleActionStrategy> strategies = new ArrayList<>();
    private LinkedHashSet<AbstractRuleActionRAO> actions = new LinkedHashSet<>();
    private ArgumentCaptor<RuleEngineResultRAO> captor = ArgumentCaptor.forClass(RuleEngineResultRAO.class);
    private List<PromotionResultModel> promotionResults = new ArrayList<>();
    private Set<AbstractPromotionActionModel> actionModels = new HashSet<>();
    @Mock
    private AbstractOrderModel order;
    @Mock
    private DroolsKIEModuleService droolsKIEModuleService;
    @Mock
    private DroolsKIEModuleModel kieModule;
    @Mock
    private DroolsKIEBaseModel kieBase;
    @Mock
    private DroolsKIESessionModel kieSession;
    @Mock
    private CustomerService customerService;

    private FixedPriceProductRAO generateFixedPriceAction(Long entryId, String ruleCode1, double fixedPrice) {
        FixedPriceProductRAO rao = new FixedPriceProductRAO();
        rao.setFiredRuleCode(ruleCode1);
        rao.setFixedPrice(BigDecimal.valueOf(fixedPrice));
        OrderEntryRAO orderEntryRAO = new OrderEntryRAO();
        orderEntryRAO.setId(entryId);
        rao.setOrderEntryRAO(orderEntryRAO);
        return rao;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        defaultPromotionEngineService = new DefaultPromotionEngineService(droolsRuleEngineContextRepository,
                commerceRuleEngineService, factContextFactory, ruleActionService,
                calculationService, promotionSourceRuleService);
        defaultPromotionEngineService.setPromotionResultService(promotionResultService);
        defaultPromotionEngineService.setModelService(modelService);
        defaultPromotionEngineService.setDroolsKIEModuleService(droolsKIEModuleService);
        when(adjustTotalActionStrategy.getStrategyId()).thenReturn("adjustTotalActionStrategy");
        strategies.add(adjustTotalActionStrategy);
        defaultPromotionEngineService.setStrategies(strategies);
        defaultPromotionEngineService.setCustomerService(customerService);
        when(ruleBasedPromotion.getPromotionDescription()).thenReturn(RULE_BASED_PROMOTION_DESC);
        when(abstractPromotion.getDescription()).thenReturn(NOT_RULE_BASED_PROMOTION_DESC);
        actions.add(discountRAO1);
        actions.add(discountRAO2);
        actions.add(discountRAO3);
        promotionResults.add(promotionResultModel);
        ruleEngineResultRAO.setActions(actions);
        RuleBasedOrderAdjustTotalActionModel adjustTotalActionModel = new RuleBasedOrderAdjustTotalActionModel();
        adjustTotalActionModel.setAmount(new BigDecimal(20000));
        adjustTotalActionModel.setStrategyId("adjustTotalActionStrategy");
        actionModels.add(adjustTotalActionModel);
        when(promotionResultModel.getActions()).thenReturn(actionModels);
        when(discountRAO1.getFiredRuleCode()).thenReturn("fireRuleCode1");
        when(discountRAO2.getFiredRuleCode()).thenReturn("fireRuleCode2");
        when(discountRAO3.getFiredRuleCode()).thenReturn("fireRuleCode3");
        when(droolsKIEModuleService.findByCompanyId(anyLong())).thenReturn(kieModule);
        when(kieModule.getDefaultKIEBase()).thenReturn(kieBase);
        when(kieBase.getDefaultKieSession()).thenReturn(kieSession);
    }


    @Test
    public void testEvaluation_fail() {
        when(factContextFactory.createFactContext(Mockito.any(FactContextType.class), Mockito.anyCollection())).thenReturn(
                factContext);
        when(factContext.getFacts()).thenReturn(Collections.emptyList());

        when(commerceRuleEngineService.evaluate(any(RuleEvaluationContext.class)))
                .thenThrow(new RuleEngineRuntimeException());
        RuleEvaluationResult result = defaultPromotionEngineService.evaluate(cart, Collections.emptyList(),
                Calendar.getInstance().getTime());
        assertTrue(result.isEvaluationFailed());
    }

    @Test
    public void testEvaluation() {
        when(factContextFactory.createFactContext(Mockito.any(FactContextType.class), Mockito.anyCollection())).thenReturn(
                factContext);
        when(factContext.getFacts()).thenReturn(Collections.emptyList());

        defaultPromotionEngineService.evaluate(cart, Collections.emptyList(), Calendar.getInstance().getTime());
        verify(commerceRuleEngineService).evaluate(any(RuleEvaluationContext.class));
    }


    @Test
    public void testEvaluationPassWhenLessThanOneContextMappedIsFoundAndOneContextByRuleModuleFound() {
        defaultPromotionEngineService.determineRuleEngineContext(order);
        verify(droolsRuleEngineContextRepository).findByKieSession(kieSession);
    }

    @Test
    public void updatePromotions_FilterActionsByPriority_AcceptHighestPriorityRunFirts() {
        when(factContextFactory.createFactContext(Mockito.any(FactContextType.class), Mockito.anyCollection()))
                .thenReturn(factContext);
        when(factContext.getFacts()).thenReturn(Collections.emptyList());
        when(commerceRuleEngineService.evaluate(any(RuleEvaluationContext.class)))
                .thenReturn(evaluationResult);
        when(evaluationResult.isEvaluationFailed()).thenReturn(false);
        when(evaluationResult.getResult()).thenReturn(ruleEngineResultRAO);
        when(promotionResultService.findAllByOrder(cart)).thenReturn(null);

        when(promotionSourceRuleService.findByCode("fireRuleCode1")).thenReturn(promotionSourceRuleModel1);
        when(promotionSourceRuleService.findByCode("fireRuleCode2")).thenReturn(promotionSourceRuleModel2);
        when(promotionSourceRuleService.findByCode("fireRuleCode3")).thenReturn(promotionSourceRuleModel3);
        when(promotionSourceRuleModel1.getPriority()).thenReturn(3);
        when(promotionSourceRuleModel2.getPriority()).thenReturn(2);
        when(promotionSourceRuleModel3.getPriority()).thenReturn(1);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());

        defaultPromotionEngineService.updatePromotions(Collections.emptyList(), cart);
        verify(ruleActionService).applyAllActions(captor.capture());
        RuleEngineResultRAO ruleEngineResultRAO = captor.getValue();
        List<AbstractRuleActionRAO> actualActions = ruleEngineResultRAO.getActions().stream().collect(Collectors.toList());
        assertEquals(3, actualActions.size());
        assertEquals("fireRuleCode1", actualActions.get(0).getFiredRuleCode());
        assertEquals("fireRuleCode2", actualActions.get(1).getFiredRuleCode());
        assertEquals("fireRuleCode3", actualActions.get(2).getFiredRuleCode());
    }

    @Test
    public void getFixedPriceProductActions_onePromotionRule_2Entries() {
        FixedPriceProductRAO fixedAction1 = generateFixedPriceAction(1l,"ruleCode1", 15000d);
        FixedPriceProductRAO fixedAction2 = generateFixedPriceAction(2l,"ruleCode1", 15000d);
        List<AbstractRuleActionRAO> fixedPriceProductActions1 = Arrays.asList(fixedAction1, fixedAction2);
        List<FixedPriceProductRAO> fixedPriceProductActions = defaultPromotionEngineService.filterCheapestFixedPriceProductAction(fixedPriceProductActions1);
        assertEquals(2, fixedPriceProductActions.size());
        assertEquals(15000d, fixedPriceProductActions.get(0).getFixedPrice().doubleValue(), 0);
        assertEquals("ruleCode1", fixedPriceProductActions.get(0).getFiredRuleCode());
        assertEquals(15000d, fixedPriceProductActions.get(1).getFixedPrice().doubleValue(), 0);
        assertEquals("ruleCode1", fixedPriceProductActions.get(1).getFiredRuleCode());
    }

    @Test
    public void getFixedPriceProductActions_OneEntryBelongTo2PromotionRule_ShouldAppliedCHEAPEST() {
        FixedPriceProductRAO fixedAction1 = generateFixedPriceAction(1l,"ruleCode1", 15000d);
        FixedPriceProductRAO fixedAction2 = generateFixedPriceAction(2l,"ruleCode1", 15000d);
        FixedPriceProductRAO fixedAction3 = generateFixedPriceAction(2l, "ruleCode2", 20000d);

        List<AbstractRuleActionRAO> fixedPriceProductActions1 = Arrays.asList(fixedAction1, fixedAction2, fixedAction3);
        List<FixedPriceProductRAO> fixedPriceProductActions = defaultPromotionEngineService.filterCheapestFixedPriceProductAction(fixedPriceProductActions1);
        assertEquals(2, fixedPriceProductActions.size());
        assertEquals(15000d, fixedPriceProductActions.get(0).getFixedPrice().doubleValue(), 0);
        assertEquals("ruleCode1", fixedPriceProductActions.get(0).getFiredRuleCode());
        assertEquals(15000d, fixedPriceProductActions.get(1).getFixedPrice().doubleValue(), 0);
        assertEquals("ruleCode1", fixedPriceProductActions.get(1).getFiredRuleCode());
    }

    @Test
    public void getFixedPriceProductActions_OneEntryBelongTo3PromotionRule_ShouldAppliedCHEAPEST() {
        FixedPriceProductRAO fixedAction1 = generateFixedPriceAction(1l, "ruleCode1", 15000d);
        FixedPriceProductRAO fixedAction2 = generateFixedPriceAction(2l, "ruleCode1", 15000d);
        FixedPriceProductRAO fixedAction3 = generateFixedPriceAction(2l, "ruleCode2", 20000d);
        FixedPriceProductRAO fixedAction4 = generateFixedPriceAction(2l, "ruleCode3", 10000d);

        List<AbstractRuleActionRAO> fixedPriceProductActions1 = Arrays.asList(fixedAction1, fixedAction2, fixedAction3, fixedAction4);
        List<FixedPriceProductRAO> fixedPriceProductActions = defaultPromotionEngineService.filterCheapestFixedPriceProductAction(fixedPriceProductActions1);
        assertEquals(15000d, fixedPriceProductActions.get(0).getFixedPrice().doubleValue(), 0);
        assertEquals("ruleCode1", fixedPriceProductActions.get(0).getFiredRuleCode());
        assertEquals(2, fixedPriceProductActions.size());
        assertEquals(10000d, fixedPriceProductActions.get(1).getFixedPrice().doubleValue(), 0);
        assertEquals("ruleCode3", fixedPriceProductActions.get(1).getFiredRuleCode());
    }


}
