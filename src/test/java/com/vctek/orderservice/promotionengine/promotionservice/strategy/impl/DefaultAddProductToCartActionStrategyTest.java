package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.PriceData;
import com.vctek.orderservice.dto.ProductInFreeGiftComboData;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderAddProductActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.OrderUtils;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.PriceType;
import com.vctek.redis.ProductData;
import org.apache.commons.collections.map.SingletonMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultAddProductToCartActionStrategyTest {
    private static final String BEAN_NAME = "defaultAddProductToCartActionStrategy";
    private static final Long PRODUCT_ID = 1234l;
    private static final int PRODUCT_QUANTITY = 1;

    private DefaultAddProductToCartActionStrategy defaultAddProductToCartActionStrategy;

    @Mock
    private FreeProductRAO freeProductRao;
    @Mock
    private AbstractActionedRAO abstractActionedRao;
    @Mock
    private CartRAO cartRao;
    @Mock
    private OrderEntryRAO orderEntryRao;
    @Mock
    private ProductRAO productRao;
    @Mock
    private ProductService productService;
    @Mock
    private PromotionActionService promotionActionService;
    @Mock
    private PromotionResultModel promotionResult;
    @Mock
    private CartModel cartMock;
    @Mock
    private CartService cartService;
    @Mock
    private CartEntryModel cartEntry;
    @Mock
    private DroolsRuleModel rule;
    @Mock
    private BillService billService;
    @Mock
    private PriceData priceDataMock;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderUtils orderUtils;
    @Mock
    private CalculationService calculationService;
    @Mock
    private ModelService modelService;

    private CartModel cartModel = new CartModel();
    private OrderModel orderModel = new OrderModel();
    private ArgumentCaptor<CommerceCartModification> commerceCartModificationCaptor;
    private ArgumentCaptor<AbstractOrderEntryModel> orderEntryCaptor;
    @Mock
    private ProductData productData;

    private AbstractOrderEntryModel generateCartEntry(Long productId, Long qty, Double price, Integer entryNumber) {
        AbstractOrderEntryModel model = new AbstractOrderEntryModel();
        model.setProductId(productId);
        model.setQuantity(qty);
        model.setBasePrice(price);
        model.setEntryNumber(entryNumber);
        return model;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        commerceCartModificationCaptor = ArgumentCaptor.forClass(CommerceCartModification.class);
        orderEntryCaptor = ArgumentCaptor.forClass(AbstractOrderEntryModel.class);
        defaultAddProductToCartActionStrategy = new DefaultAddProductToCartActionStrategy(modelService,
                promotionActionService, calculationService);
        defaultAddProductToCartActionStrategy.setBillService(billService);
        defaultAddProductToCartActionStrategy.setBeanName(BEAN_NAME);
        defaultAddProductToCartActionStrategy.setCartService(cartService);
        defaultAddProductToCartActionStrategy.setOrderService(orderService);
        defaultAddProductToCartActionStrategy.setOrderUtils(orderUtils);
        defaultAddProductToCartActionStrategy.setProductService(productService);

        when(freeProductRao.getAppliedToObject()).thenReturn(cartRao);
        when(freeProductRao.getAddedOrderEntry()).thenReturn(orderEntryRao);
        when(orderEntryRao.getProduct()).thenReturn(productRao);
        when(promotionActionService.createPromotionResult(freeProductRao)).thenReturn(promotionResult);
        when(promotionResult.getOrder()).thenReturn(cartMock);
        when(productRao.getId()).thenReturn(PRODUCT_ID);
        when(priceDataMock.getPrice()).thenReturn(20000d);
        when(productService.getPriceOfProduct(PRODUCT_ID, 0)).thenReturn(priceDataMock);
        when(cartService.addNewEntry(cartMock, PRODUCT_ID, PRODUCT_QUANTITY, false)).thenReturn(cartEntry);
        when(cartEntry.getEntryNumber()).thenReturn(Integer.valueOf(0));
        when(cartEntry.getOrder()).thenReturn(cartModel);
        when(promotionActionService.getRule(freeProductRao)).thenReturn(rule);
        when(Integer.valueOf(orderEntryRao.getQuantity())).thenReturn(Integer.valueOf(PRODUCT_QUANTITY));
        when(productService.getBasicProductDetail(anyLong())).thenReturn(productData);
        cartModel.setCompanyId(1l);
        when(promotionActionService.getOrder(freeProductRao)).thenReturn(cartModel);
    }

    @Test
    public void testApplyNotFreeProductRAO() {
        final List result = defaultAddProductToCartActionStrategy.apply(new AbstractRuleActionRAO());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyAppliedToObjectNotCartRAO() {
        when(freeProductRao.getAppliedToObject()).thenReturn(abstractActionedRao);

        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyAddedOrderEntryNull() {
        when(freeProductRao.getAddedOrderEntry()).thenReturn(null);

        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyProductNull() {
        when(orderEntryRao.getProduct()).thenReturn(null);

        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyProductCodeNull() {
        when(productRao.getId()).thenReturn(null);

        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyPromotionResultNull() {
        when(promotionActionService.createPromotionResult(freeProductRao)).thenReturn(null);

        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyOrderNull() {
        when(promotionResult.getOrder()).thenReturn(null);
        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApply() {
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(PRODUCT_ID);
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setOrder(orderModel);
        when(promotionResult.getOrder()).thenReturn(orderModel);
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(new ProductIsCombo());
        when(orderService.addNewEntry(orderModel, PRODUCT_ID, 1l, true))
                .thenReturn(orderEntryModel);
        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(promotionResult, result.get(0));
    }

    @Test
    public void testApplyWithOrder() {
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(PRODUCT_ID);
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setOrder(orderModel);

        when(promotionResult.getOrder()).thenReturn(orderModel);
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(new ProductIsCombo());
        when(orderService.addNewEntry(orderModel, PRODUCT_ID, 1l, true))
                .thenReturn(orderEntryModel);
        when(productService.getPriceOfProduct(PRODUCT_ID, 0))
                .thenReturn(priceDataMock);
        when(billService.shouldUpdateBillOf(orderModel)).thenReturn(true);

        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        verify(orderService).addNewEntry(orderModel, PRODUCT_ID, 1l, true);
        verify(billService).addProductToReturnBill(eq(orderModel), orderEntryCaptor.capture());
        AbstractOrderEntryModel orderEntryCaptorValue = orderEntryCaptor.getValue();
        assertEquals(20000d, orderEntryCaptorValue.getBasePrice(), 0);
        assertEquals(PRODUCT_ID, orderEntryCaptorValue.getProductId(), 0);
        assertEquals(true, orderEntryCaptorValue.isGiveAway());
        assertEquals(1, orderEntryCaptorValue.getQuantity(), 0);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(promotionResult, result.get(0));
    }

    @Test
    public void testApplyWithOrder_WholesalePriceType() {
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(PRODUCT_ID);
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setOrder(orderModel);
        orderModel.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        when(promotionResult.getOrder()).thenReturn(orderModel);
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(new ProductIsCombo());
        when(orderService.addNewEntry(orderModel, PRODUCT_ID, 1l, true))
                .thenReturn(orderEntryModel);
        when(productService.getPriceOfProduct(PRODUCT_ID, 0))
                .thenReturn(priceDataMock);
        when(billService.shouldUpdateBillOf(orderModel)).thenReturn(true);
        when(priceDataMock.getWholesalePrice()).thenReturn(21000d);

        final List result = defaultAddProductToCartActionStrategy.apply(freeProductRao);
        verify(orderService).addNewEntry(orderModel, PRODUCT_ID, 1l, true);
        verify(billService).addProductToReturnBill(eq(orderModel), orderEntryCaptor.capture());
        AbstractOrderEntryModel orderEntryCaptorValue = orderEntryCaptor.getValue();
        assertEquals(21000d, orderEntryCaptorValue.getBasePrice(), 0);
        assertEquals(PRODUCT_ID, orderEntryCaptorValue.getProductId(), 0);
        assertEquals(true, orderEntryCaptorValue.isGiveAway());
        assertEquals(1, orderEntryCaptorValue.getQuantity(), 0);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(promotionResult, result.get(0));
    }

    @Test
    public void testCreatePromotionAction() {
        final RuleBasedOrderAddProductActionModel action = defaultAddProductToCartActionStrategy
                .createOrderAddProductAction(freeProductRao, PRODUCT_QUANTITY, PRODUCT_ID, promotionResult);

        assertEquals(promotionResult, action.getPromotionResult());
        assertEquals(rule, action.getRule());
        assertEquals(BEAN_NAME, action.getStrategyId());
        assertEquals(PRODUCT_QUANTITY, action.getQuantity(), 0);
        assertEquals(PRODUCT_ID, action.getProductId());
    }

    @Test
    public void testUndo() {
        final RuleBasedOrderAddProductActionModel action = new RuleBasedOrderAddProductActionModel();
        action.setProductId(PRODUCT_ID);
        action.setPromotionResult(promotionResult);
        action.setQuantity(1);

        cartModel.setCurrencyCode("USD");
        cartModel.setCode("1234");
        cartModel.getEntries().add(generateCartEntry(123456l, 1l, 30.00, 0));
        cartModel.getEntries().add(generateCartEntry(PRODUCT_ID, 1l, 20.00, 1));
        cartModel.getEntries().add(generateCartEntry(234567l, 2l, 60.00, 2));


        // check entry numbers prior to undo
        assertEquals(3, cartModel.getEntries().size());
        assertEquals(0, cartModel.getEntries().get(0).getEntryNumber().intValue());
        assertEquals(1, cartModel.getEntries().get(1).getEntryNumber().intValue());
        assertEquals(2, cartModel.getEntries().get(2).getEntryNumber().intValue());

        for (final AbstractOrderEntryModel entry : cartModel.getEntries()) {
            // set 2nd entry as the free gift.
            if (entry.getEntryNumber().intValue() == 1) {
                entry.setProductId(PRODUCT_ID);
                entry.setGiveAway(Boolean.TRUE);
            }
        }

        doAnswer(new UpdateQuantitiesAnswer()).when(cartService).updateQuantities(cartModel,
                new SingletonMap(Integer.valueOf(1), Long.valueOf(0)));
        when(promotionResult.getOrder()).thenReturn(cartModel);
        // removes free gift and normalizes order entry numbers
        defaultAddProductToCartActionStrategy.undo(action);

        assertEquals(2, cartModel.getEntries().size());
        assertEquals(0, cartModel.getEntries().get(0).getEntryNumber().intValue());
        assertEquals(1, cartModel.getEntries().get(1).getEntryNumber().intValue());
        assertEquals(123456l, cartModel.getEntries().get(0).getProductId(), 0);
        assertEquals(234567l, cartModel.getEntries().get(1).getProductId(), 0);
    }


    private class UpdateQuantitiesAnswer implements Answer {

        @Override
        public Object answer(final InvocationOnMock invocation) {
            final AbstractOrderModel arg1 = (AbstractOrderModel) invocation.getArguments()[0];
            for (final Iterator<AbstractOrderEntryModel> iter = arg1.getEntries().listIterator(); iter.hasNext(); ) {
                final AbstractOrderEntryModel entry = iter.next();
                if (entry.getEntryNumber().intValue() == 1) {
                    iter.remove();
                }
            }
            return arg1;
        }
    }

    @Test
    public void testUndo_WithOrder_ShouldRevertProductInBill() {
        final RuleBasedOrderAddProductActionModel action = new RuleBasedOrderAddProductActionModel();
        action.setProductId(PRODUCT_ID);
        action.setPromotionResult(promotionResult);
        action.setQuantity(1);

        orderModel.setCurrencyCode("USD");
        orderModel.setCode("1234");
        orderModel.getEntries().add(generateCartEntry(123456l, 1l, 30.00, 0));
        orderModel.getEntries().add(generateCartEntry(PRODUCT_ID, 1l, 20.00, 1));
        orderModel.getEntries().add(generateCartEntry(234567l, 2l, 60.00, 2));

        // check entry numbers prior to undo
        assertEquals(3, orderModel.getEntries().size());
        assertEquals(0, orderModel.getEntries().get(0).getEntryNumber().intValue());
        AbstractOrderEntryModel removedEntry = orderModel.getEntries().get(1);
        assertEquals(1, removedEntry.getEntryNumber().intValue());
        assertEquals(2, orderModel.getEntries().get(2).getEntryNumber().intValue());

        for (final AbstractOrderEntryModel entry : orderModel.getEntries()) {
            // set 2nd entry as the free gift.
            if (entry.getEntryNumber().intValue() == 1) {
                entry.setProductId(PRODUCT_ID);
                entry.setGiveAway(Boolean.TRUE);
            }
        }

        doAnswer(new UpdateQuantitiesAnswer()).when(orderUtils).updateOrderQuantities(orderModel,
                new SingletonMap(Integer.valueOf(1), Long.valueOf(0)));
        when(promotionResult.getOrder()).thenReturn(orderModel);
        when(billService.shouldUpdateBillOf(orderModel)).thenReturn(true);
        // removes free gift and normalizes order entry numbers
        defaultAddProductToCartActionStrategy.undo(action);
        verify(billService).deleteProductInReturnBillWithOrder(eq(orderModel), commerceCartModificationCaptor.capture());
        CommerceCartModification cartModification = commerceCartModificationCaptor.getValue();
        assertEquals(removedEntry, cartModification.getEntry());
        assertEquals(PRODUCT_ID, cartModification.getProductId());
        assertEquals(orderModel, cartModification.getOrder());
        assertEquals(2, orderModel.getEntries().size());
        assertEquals(0, orderModel.getEntries().get(0).getEntryNumber().intValue());
        assertEquals(1, orderModel.getEntries().get(1).getEntryNumber().intValue());
        assertEquals(123456l, orderModel.getEntries().get(0).getProductId(), 0);
        assertEquals(234567l, orderModel.getEntries().get(1).getProductId(), 0);
    }

    @Test
    public void testUndoNoUndoEntryFound() {
        final RuleBasedOrderAddProductActionModel action = new RuleBasedOrderAddProductActionModel();
        action.setProductId(PRODUCT_ID);
        action.setPromotionResult(promotionResult);
        action.setQuantity(1);

        final CartModel cart = new CartModel();
        cart.setCurrencyCode("USD");
        cart.setCode("1234");
        CartEntryModel entry1 = new CartEntryModel();
        entry1.setProductId(123456l);
        entry1.setQuantity(1L);
        entry1.setBasePrice(30.00);
        entry1.setEntryNumber(0);

        CartEntryModel entry2 = new CartEntryModel();
        entry2.setProductId(2222l);
        entry2.setQuantity(1L);
        entry2.setBasePrice(20.00);
        entry2.setEntryNumber(1);
        cart.getEntries().add(entry1);
        cart.getEntries().add(entry2);
        when(promotionResult.getOrder()).thenReturn(cart);

        defaultAddProductToCartActionStrategy.undo(action);
        assertEquals(2, cart.getEntries().size());
    }

    @Test
    public void removeProductInComboOfOrder_EmptyMap() {
        defaultAddProductToCartActionStrategy.removeProductInComboOfOrder("orderCode");
        assertEquals(0, defaultAddProductToCartActionStrategy.getComboWithProductMap().size());
    }

    @Test
    public void removeProductInComboOfOrder_WithOrderCode() {
        Map<String, List<ProductInFreeGiftComboData>> comboWithProductMap = defaultAddProductToCartActionStrategy.getComboWithProductMap();
        String key1 = defaultAddProductToCartActionStrategy.generateComboFreeGiftKey("1234", 2222l);
        String key2 = defaultAddProductToCartActionStrategy.generateComboFreeGiftKey("12345", 2222l);
        String key3 = defaultAddProductToCartActionStrategy.generateComboFreeGiftKey("1233", 2222l);
        comboWithProductMap.put(key1, Arrays.asList(new ProductInFreeGiftComboData()));
        comboWithProductMap.put(key2, Arrays.asList(new ProductInFreeGiftComboData()));
        comboWithProductMap.put(key3, Arrays.asList(new ProductInFreeGiftComboData()));

        defaultAddProductToCartActionStrategy.removeProductInComboOfOrder("1234");

        assertEquals(2, comboWithProductMap.size());
        assertTrue(comboWithProductMap.containsKey(key2));
        assertTrue(comboWithProductMap.containsKey(key3));
    }
}
