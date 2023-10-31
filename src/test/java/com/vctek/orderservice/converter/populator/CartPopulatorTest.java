package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.CartData;
import com.vctek.orderservice.dto.OrderEntryData;
import com.vctek.orderservice.dto.ValidCouponCodeData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.CouponService;
import com.vctek.orderservice.service.PaymentTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CartPopulatorTest {

    @Mock
    private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
    @Mock
    private PromotionResultService promotionResultService;
    private CartPopulator populator;
    private CartModel cart = new CartModel();
    private CartData cartData = new CartData();
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();
    @Mock
    private PaymentTransactionService paymentTransactionService;
    @Mock
    private RuleConditionsService ruleConditionsService;
    @Mock
    private RuleConditionsRegistry ruleConditionsRegistry;

    @Mock
    private CalculationService calculationService;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private CouponService couponService;
    @Mock
    private ObjectMapper objectMapper;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new CartPopulator();
        populator.setPromotionResultService(promotionResultService);
        populator.setOrderEntryConverter(orderEntryConverter);
        populator.setPaymentTransactionService(paymentTransactionService);
        populator.setRuleConditionsRegistry(ruleConditionsRegistry);
        populator.setRuleConditionsService(ruleConditionsService);
        populator.setCalculationService(calculationService);
        populator.setEntryRepository(entryRepository);
        populator.setCouponService(couponService);
        populator.setObjectMapper(objectMapper);
        when(entryRepository.findAllByOrder(cart)).thenReturn(entries);
        cart.setEntries(entries);
        when(couponService.getValidatedCouponCode(cart)).thenReturn(new ValidCouponCodeData());
    }

    @Test
    public void populate() {
        cart.setCompanyId(1l);
        cart.setWarehouseId(3l);
        cart.setCode("2l");
        cart.setFinalPrice(20000d);
        cart.setTotalRewardAmount(3000d);
        cart.setRewardPoint(3d);
        populator.populate(cart, cartData);
        verify(orderEntryConverter).convertAll(anyList());
        assertEquals(1l, cartData.getCompanyId(), 0);
        assertEquals("2l", cartData.getCode());
        assertEquals(20000d, cartData.getFinalPrice(), 0);
        assertEquals(3000, cartData.getTotalRewardAmount(),0);
        assertEquals(3, cartData.getRewardPoint(),0);
        verify(couponService).getValidatedCouponCode(cart);
    }

}
