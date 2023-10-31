package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.ReturnOrderData;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BasicReturnOrderPopulatorTest {
    private BasicReturnOrderPopulator populator;
    @Mock
    private ReturnOrderModel model;
    @Mock
    private ReturnOrderData target;
    @Mock
    private OrderModel order;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private LoyaltyCardData loyaltyCardData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new BasicReturnOrderPopulator();
        populator.setLoyaltyService(loyaltyService);
    }

    @Test
    public void populate() {
        when(model.getId()).thenReturn(1l);
        when(model.getBillId()).thenReturn(1l);
        when(model.getNote()).thenReturn("note");
        when(model.getOriginOrder()).thenReturn(order);
        when(model.getConversionRate()).thenReturn(1000d);
        when(model.getCompensateRevert()).thenReturn(10000d);
        when(order.getCode()).thenReturn("code");
        when(order.getType()).thenReturn(OrderType.ONLINE.toString());
        when(order.getCustomerId()).thenReturn(1l);

        populator.populate(model, target);
        verify(target).setId(anyLong());
        verify(target).setBillId(anyLong());
        verify(target).setNote(anyString());
        verify(target).setId(anyLong());
        verify(target).setOriginOrderCode(anyString());
        verify(target).setOriginOrderType(anyString());
        verify(target).setCustomerId(anyLong());
        verify(target).setConversionRate(1000d);
        verify(target).setCompensateRevert(10000d);
    }

    @Test
    public void populateRefund(){
        when(model.getOriginOrder()).thenReturn(order);
        when(model.getConversionRate()).thenReturn(1000d);
        when(order.getCardNumber()).thenReturn("card number");
        when(order.getCompanyId()).thenReturn(1L);
        when(loyaltyService.findByCardNumber("card number",1L)).thenReturn(loyaltyCardData);
        when(loyaltyCardData.getPendingAmount()).thenReturn(1d);
        when(loyaltyCardData.getPointAmount()).thenReturn(2d);
        when(model.getRefundAmount()).thenReturn(1000d);
        when(model.getRevertAmount()).thenReturn(null);
        populator.populate(model, target);
        verify(target).setConversionRate(1000d);
        verify(target).setPendingPoint(1d);
        verify(target).setAvailablePoint(2d);
        verify(target).setRefundPoint(1d);
        verify(target,times(0)).setRevertPoint(anyDouble());
    }

    @Test
    public void populateRevert(){
        when(model.getOriginOrder()).thenReturn(order);
        when(model.getConversionRate()).thenReturn(1000d);
        when(order.getCardNumber()).thenReturn("card number");
        when(order.getCompanyId()).thenReturn(1L);
        when(loyaltyService.findByCardNumber("card number",1L)).thenReturn(loyaltyCardData);
        when(loyaltyCardData.getPendingAmount()).thenReturn(1d);
        when(loyaltyCardData.getPointAmount()).thenReturn(2d);
        when(model.getRevertAmount()).thenReturn(1000d);
        when(model.getRefundAmount()).thenReturn(null);
        populator.populate(model, target);
        verify(target).setConversionRate(1000d);
        verify(target).setPendingPoint(1d);
        verify(target).setAvailablePoint(2d);
        verify(target).setRevertPoint(1d);
        verify(target,times(0)).setRefundPoint(anyDouble());
    }

    @Test
    public void populateBothRefundAndRevertAndRedeem(){
        when(model.getOriginOrder()).thenReturn(order);
        when(model.getConversionRate()).thenReturn(1000d);
        when(order.getCardNumber()).thenReturn("card number");
        when(order.getCompanyId()).thenReturn(1L);
        when(loyaltyService.findByCardNumber("card number",1L)).thenReturn(loyaltyCardData);
        when(loyaltyCardData.getPendingAmount()).thenReturn(1d);
        when(loyaltyCardData.getPointAmount()).thenReturn(2d);
        when(model.getRefundAmount()).thenReturn(1000d);
        when(model.getRevertAmount()).thenReturn(2222d);
        when(model.getRedeemAmount()).thenReturn(3333d);
        populator.populate(model, target);
        verify(target).setConversionRate(1000d);
        verify(target).setPendingPoint(1d);
        verify(target).setAvailablePoint(2d);
        verify(target).setRefundPoint(1d);
        verify(target).setRevertPoint(2.222d);
        verify(target).setRedeemPoint(3.333d);
    }

}
