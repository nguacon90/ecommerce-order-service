package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractPromotionActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractRuleBasedPromotionActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedPromotionModel;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CalculationException;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import com.vctek.orderservice.repository.CartEntryRepository;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class DefaultPromotionActionServiceTest {

    private static final String MODULE_NAME = "MODULE_NAME";
    private static final Long CART_CODE = 111l;
    private static final String RULE_CODE = "ruleCode";

    private DefaultPromotionActionService defaultPromotionActionService;
    @Mock
    private ModelService modelService;
    @Mock
    private AbstractOrderModel cart;
    @Mock
    private CartRAO cartRAO;
    @Mock
    private DroolsRuleModel rule;
    @Mock
    private RuleBasedPromotionModel promotion;
    @Mock
    private DiscountRAO discountRAO;
    @Mock
    private AbstractRuleBasedPromotionActionModel action1;
    @Mock
    private AbstractRuleActionRAO actionAppliedToCart;
    @Mock
    private OrderEntryRAO orderEntryRaoWithInvalidOrder;
    @Mock
    private OrderEntryRAO orderEntryRaoWithInvalidOrder2;
    @Mock
    private AbstractOrderRAO order;
    @Mock
    private AbstractOrderRAO invalidOrder;
    @Mock
    private ProductRAO product;
    @Mock
    private PromotionResultModel promotionResult;
    @Mock
    private AbstractOrderEntryModel orderEntry;
    @Mock
    private DroolsRuleService droolService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private CartEntryRepository cartEntryRepository;

    @Mock
    private OrderEntryRepository orderEntryRepository;

    private List<DiscountValue> discountValues;
    private List<AbstractOrderEntryModel> orderEntries;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        discountValues = new ArrayList<>();
        orderEntries = new ArrayList<>();
        defaultPromotionActionService = new DefaultPromotionActionService(modelService, droolService, calculationService);
        defaultPromotionActionService.setCartEntryRepository(cartEntryRepository);
        defaultPromotionActionService.setOrderEntryRepository(orderEntryRepository);
        when(cart.getPromotionResults()).thenReturn(new HashSet<>());
        when(droolService.findByCodeAndActive(anyString(), anyBoolean())).thenReturn(rule);
        when(rule.getPromotion()).thenReturn(promotion);
        when(discountRAO.getAppliedToObject()).thenReturn(cartRAO);
        when(discountRAO.getFiredRuleCode()).thenReturn(RULE_CODE);
        when(discountRAO.getModuleName()).thenReturn(MODULE_NAME);
        when(cartRAO.getId()).thenReturn(CART_CODE);

        when(actionAppliedToCart.getAppliedToObject()).thenReturn(new CartRAO());

        when(invalidOrder.getCode()).thenReturn("unknownCartCode");
        when(orderEntryRaoWithInvalidOrder.getOrder()).thenReturn(invalidOrder);
        when(orderEntryRaoWithInvalidOrder.getProduct()).thenReturn(product);

        when(order.getId()).thenReturn(CART_CODE);
        when(orderEntryRaoWithInvalidOrder2.getOrder()).thenReturn(order);
        when(orderEntryRaoWithInvalidOrder2.getEntryNumber()).thenReturn(Integer.valueOf(1));
        when(orderEntryRaoWithInvalidOrder2.getProduct()).thenReturn(product);
        when(modelService.findById(eq(AbstractOrderModel.class), anyLong())).thenReturn(cart);

    }

    /**
     * tests that a promotionResultModel gets created with the default values
     */
    @Test
    public void testCreatePromotionResult() {
        when(cart.getPromotionResults()).thenReturn(new HashSet<>());
        final PromotionResultModel newPromotionResult = defaultPromotionActionService.createPromotionResult(discountRAO);
        assertEquals(1.0, newPromotionResult.getCertainty(), 0);
        assertEquals(promotion, newPromotionResult.getPromotion());
        assertEquals(cart, newPromotionResult.getOrder());
    }

    /**
     * tests that if there is an existing promotionResultModel created by the same rule, the existing one is returned
     */
    @Test
    public void testCreatePromotionResultForExistingPromotionResult() {
        final Set<AbstractPromotionActionModel> actions = Collections.singleton(action1);
        PromotionResultModel promotionResultModel = new PromotionResultModel();
        promotionResultModel.setId(promotionResult.getId());
        promotionResultModel.setActions(actions);
        final Set<PromotionResultModel> allPromotionResults = new HashSet<>(Arrays.asList(promotionResultModel));
        when(cart.getPromotionResults()).thenReturn(allPromotionResults);
        when(action1.getRule()).thenReturn(rule);

        final PromotionResultModel newPromotionResult = defaultPromotionActionService.createPromotionResult(discountRAO);

        assertEquals(1.0, newPromotionResult.getCertainty(), 0);
        assertEquals(promotion, newPromotionResult.getPromotion());
        assertEquals(cart, newPromotionResult.getOrder());
        assertEquals(newPromotionResult, promotionResultModel);
    }

    @Test
    public void testTryToGetOrderEntryFromImproperAction() {
        when(modelService.findById(eq(AbstractOrderModel.class), anyLong())).thenReturn(new CartModel());
        assertNull(defaultPromotionActionService.getOrderEntry(actionAppliedToCart));
        assertNull(defaultPromotionActionService.getOrderEntry(orderEntryRaoWithInvalidOrder));
        assertNull(defaultPromotionActionService.getOrderEntry(orderEntryRaoWithInvalidOrder2));
    }

    @Test
    public void testTryToGetOrderFromAction() {
        assertNotNull(defaultPromotionActionService.getOrder(discountRAO));
        assertNull(defaultPromotionActionService.getOrder(actionAppliedToCart));
    }

    @Test
    public void createDiscountValue() {
        when(discountRAO.getValue()).thenReturn(new BigDecimal(200));
        defaultPromotionActionService.createDiscountValue(discountRAO, UUID.randomUUID().toString(), cart);
        verify(cart).setCalculated(false);
        verify(cart).setDiscountValues(anyList());

    }

    @Test
    public void recalculateTotals_HasException() {
        doThrow(new CalculationException("")).when(calculationService).calculateTotals(cart, true);
        defaultPromotionActionService.recalculateTotals(cart);
        verify(cart).setCalculated(false);
        verify(modelService).save(cart);
    }

    /**
     * Test remove both order and order entry levels
     */
    @Test
    public void removeDiscountValue() {
        String code = UUID.randomUUID().toString();
        DiscountValue discountValue = new DiscountValue(code, 100, true, CurrencyIsoCode.VND.toString());
        orderEntries.add(orderEntry);
        when(cart.getEntries()).thenReturn(orderEntries);
        when(orderEntry.getDiscountValues()).thenReturn(discountValues);
        discountValues.add(discountValue);
        when(cart.getDiscountValues()).thenReturn(discountValues);

        List<ItemModel> itemModels = defaultPromotionActionService.removeDiscountValue(code, cart);
        assertEquals(2, itemModels.size());
    }
}