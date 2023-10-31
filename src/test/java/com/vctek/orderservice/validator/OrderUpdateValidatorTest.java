package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.FinanceService;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderUpdateValidatorTest {
    private OrderUpdateValidator validator;
    @Mock
    private OrderService orderServiceMock;

    @Mock
    private FinanceService financeService;

    private OrderRequest orderRequest = new OrderRequest();

    @Mock
    private PaymentTransactionRequest loyaltyPaymentMock;
    @Mock
    private PaymentMethodData loyaltyPaymentDataMock;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private AvailablePointAmountData availableAmountMock;
    @Mock
    private OrderModel orderMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new OrderUpdateValidator(orderServiceMock);
        validator.setFinanceService(financeService);
        validator.setLoyaltyService(loyaltyService);
        when(loyaltyPaymentMock.getPaymentMethodId()).thenReturn(2l);
        when(loyaltyPaymentDataMock.getId()).thenReturn(2l);
        orderRequest.setCardNumber("cardNumber");
        orderRequest.setCode("orderCode");
        orderRequest.setCompanyId(1l);
        when(availableAmountMock.getConversionRate()).thenReturn(1000d);
        when(loyaltyService.computeAvailablePointAmountOf(any())).thenReturn(availableAmountMock);
        when(orderServiceMock.findByCodeAndCompanyId(any(), any())).thenReturn(orderMock);
    }

    @Test
    public void validateLoyaltyPoint_emptyCardButHasRedeemAmount_ShouldThrowException() {
        try {
            when(loyaltyPaymentMock.getAmount()).thenReturn(2000d);
            orderRequest.setCardNumber(null);
            validator.validateLoyaltyPoint(orderRequest, loyaltyPaymentMock, loyaltyPaymentDataMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_LOYALTY_CARD_NUMBER.message(), e.getMessage());
        }
    }

    @Test
    public void validateLoyaltyPoint_newRedeemPointOverAvailablePoint_ShouldThrowException() {
        try {
            when(loyaltyPaymentMock.getAmount()).thenReturn(5000d);
            when(availableAmountMock.getAvailableAmount()).thenReturn(1000d);
            when(orderMock.getRedeemAmount()).thenReturn(3000d);
            validator.validateLoyaltyPoint(orderRequest, loyaltyPaymentMock, loyaltyPaymentDataMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_EXCEED_AVAILABLE_POINTS.message(), e.getMessage());
        }
    }

    @Test
    public void validateLoyaltyPoint_newRedeemPointOverPointToRedeemOnOrder_ShouldThrowException() {
        try {
            when(loyaltyPaymentMock.getAmount()).thenReturn(5000d);
            when(availableAmountMock.getAvailableAmount()).thenReturn(1d);
            when(availableAmountMock.getPointAmount()).thenReturn(4d);
            when(orderMock.getRedeemAmount()).thenReturn(3000d);

            validator.validateLoyaltyPoint(orderRequest, loyaltyPaymentMock, loyaltyPaymentDataMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_POINT_FOR_ORDER.message(), e.getMessage());
        }
    }

    @Test
    public void validateLoyaltyPoint_newRedeemPointIsNotInteger_ShouldThrowException() {
        try {
            when(loyaltyPaymentMock.getAmount()).thenReturn(5300d);
            when(availableAmountMock.getAvailableAmount()).thenReturn(6d);
            when(availableAmountMock.getPointAmount()).thenReturn(6d);
            when(orderMock.getRedeemAmount()).thenReturn(3000d);

            validator.validateLoyaltyPoint(orderRequest, loyaltyPaymentMock, loyaltyPaymentDataMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_INTEGER_POINT_FOR_ORDER.message(), e.getMessage());
        }
    }
}
