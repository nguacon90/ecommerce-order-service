package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.MoneySourceData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.util.CardStatus;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ReturnOrderRequestValidatorTest {
    private ReturnOrderRequestValidator validator;

    @Mock
    private OrderService orderService;
    @Mock
    private CartService cartService;
    @Mock
    private AuthService authService;
    @Mock
    private FinanceService financeService;
    private ReturnOrderRequest request = new ReturnOrderRequest();
    @Mock
    private OrderModel order;
    @Mock
    private OrderEntryModel entry;
    @Mock
    private PaymentTransactionRequest paymentTransactionRequest;
    private ReturnOrderEntryRequest returnOrderEntryRequest = new ReturnOrderEntryRequest();
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();
    private List<ReturnOrderEntryRequest> returnOrderEntries = new ArrayList<>();
    private List<PaymentTransactionRequest> payments = new ArrayList<>();
    @Mock
    private MoneySourceData moneySourceData;

    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private ProductService productService;
    private CartModel cartModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        returnOrderEntries.add(returnOrderEntryRequest);
        entries.add(entry);
        request.setPayments(payments);
        when(order.getEntries()).thenReturn(entries);
        when(entry.getEntryNumber()).thenReturn(0);
        when(entry.getId()).thenReturn(0l);
        when(entry.getQuantity()).thenReturn(3l);
        when(authService.getCurrentUserId()).thenReturn(2l);
        validator = new ReturnOrderRequestValidator();
        validator.setAuthService(authService);
        validator.setCartService(cartService);
        validator.setFinanceService(financeService);
        validator.setOrderService(orderService);
        validator.setLoyaltyService(loyaltyService);
        validator.setProductService(productService);

        cartModel = new CartModel();
        cartModel.setCompanyId(2l);
        cartModel.setCode("exchangeCartCode");
        cartModel.setEntries(Arrays.asList(entry));
    }

    @Test
    public void validate_emptyCompanyId() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyOriginOrderCode() {
        try {
            request.setCompanyId(1l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORIGIN_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidOriginOrderCode() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORIGIN_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void validate_noteOverMaxLength() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setNote(OrderRequestValidatorTest.NOTE + OrderRequestValidatorTest.NOTE);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOTE_OVER_MAX_LENGTH.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyOrderEntry() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setNote(OrderRequestValidatorTest.NOTE);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_RETURN_ORDER_ENTRY.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidOrderEntryNumber() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setReturnOrderEntries(returnOrderEntries);
            returnOrderEntryRequest.setEntryNumber(2);
            returnOrderEntryRequest.setOrderEntryId(2l);
            request.setNote(OrderRequestValidatorTest.NOTE);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ENTRY_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidOrderEntryQuantity_SmallerThan0() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setReturnOrderEntries(returnOrderEntries);
            returnOrderEntryRequest.setEntryNumber(0);
            returnOrderEntryRequest.setOrderEntryId(0l);
            returnOrderEntryRequest.setQuantity(-1);
            request.setNote(OrderRequestValidatorTest.NOTE);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_RETURN_ORDER_ENTRY_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidOrderEntryQuantity_LargerThanCurrentQty() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            request.setNote(OrderRequestValidatorTest.NOTE);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setReturnOrderEntries(returnOrderEntries);
            returnOrderEntryRequest.setEntryNumber(0);
            returnOrderEntryRequest.setOrderEntryId(0l);
            returnOrderEntryRequest.setQuantity(4);

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_RETURN_ORDER_ENTRY_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validate_exchangeCart_emptyCode() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            request.setNote(OrderRequestValidatorTest.NOTE);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setReturnOrderEntries(returnOrderEntries);
            returnOrderEntryRequest.setEntryNumber(0);
            returnOrderEntryRequest.setOrderEntryId(0l);
            returnOrderEntryRequest.setQuantity(3);
            request.setExchange(true);
            request.setExchangeCartCode(null);

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_EXCHANGE_CART_CODE.code(), e.getCode());
        }
    }

    @Test
    public void validate_exchangeCart_notExistedCode() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            request.setNote(OrderRequestValidatorTest.NOTE);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setReturnOrderEntries(returnOrderEntries);
            returnOrderEntryRequest.setEntryNumber(0);
            returnOrderEntryRequest.setOrderEntryId(0l);
            returnOrderEntryRequest.setQuantity(3);
            request.setExchange(true);
            request.setExchangeCartCode("exchangeCartCode");
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(null);

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_EXCHANGE_CART_CODE.code(), e.getCode());
        }
    }

    @Test
    public void validate_payment_NotValidMoneySource() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            request.setNote(OrderRequestValidatorTest.NOTE);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setReturnOrderEntries(returnOrderEntries);
            returnOrderEntryRequest.setEntryNumber(0);
            returnOrderEntryRequest.setOrderEntryId(0l);
            returnOrderEntryRequest.setQuantity(3);
            request.setExchange(true);
            request.setExchangeCartCode("exchangeCartCode");
            payments.add(paymentTransactionRequest);
            when(paymentTransactionRequest.getMoneySourceId()).thenReturn(1l);
            when(paymentTransactionRequest.getCompanyId()).thenReturn(1l);

            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
            when(financeService.getMoneySource(anyLong(), anyLong())).thenReturn(null);

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_MONEY_SOURCE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_payment_EmptyWarehouseIdWithCash() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            request.setNote(OrderRequestValidatorTest.NOTE);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            request.setReturnOrderEntries(returnOrderEntries);
            returnOrderEntryRequest.setEntryNumber(0);
            returnOrderEntryRequest.setOrderEntryId(0l);
            returnOrderEntryRequest.setQuantity(3);
            request.setExchange(true);
            request.setExchangeCartCode("exchangeCartCode");
            payments.add(paymentTransactionRequest);
            when(paymentTransactionRequest.getMoneySourceId()).thenReturn(1l);
            when(paymentTransactionRequest.getCompanyId()).thenReturn(1l);
            when(paymentTransactionRequest.getWarehouseId()).thenReturn(null);
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
            when(financeService.getMoneySource(anyLong(), anyLong())).thenReturn(moneySourceData);
            when(moneySourceData.getType()).thenReturn("CASH");
            when(paymentTransactionRequest.getAmount()).thenReturn(10000d);

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_CASH_PAYMENT_WAREHOUSE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_OrderOnlineNotComplete() {
        try {
            request.setCompanyId(1l);
            request.setOriginOrderCode("originOrderCode");
            request.setNote(OrderRequestValidatorTest.NOTE);
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
            when(order.getType()).thenReturn(OrderType.ONLINE.toString());
            when(order.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED.code());

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_CREATE_RETURN_FOR_UNCOMPLETED_ONLINE_ORDER.code(), e.getCode());
        }
    }


    @Test
    public void validate_success_withPayment() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");
        payments.add(paymentTransactionRequest);
        when(paymentTransactionRequest.getMoneySourceId()).thenReturn(1l);
        when(paymentTransactionRequest.getCompanyId()).thenReturn(1l);
        when(paymentTransactionRequest.getWarehouseId()).thenReturn(2l);
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
        when(financeService.getMoneySource(anyLong(), anyLong())).thenReturn(moneySourceData);
        when(moneySourceData.getType()).thenReturn("CASH");
        when(paymentTransactionRequest.getAmount()).thenReturn(10000d);

        validator.validate(request);
    }

    @Test
    public void validate_success_withEmptyPayment() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);

        validator.validate(request);
    }

    @Test
    public void validate_success_wittActiveCard() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
        LoyaltyCardData data = new LoyaltyCardData();
        data.setStatus(CardStatus.ACTIVE.code());
        data.setAssignedPhone("0987654321");
        when(loyaltyService.findByCardNumber(anyString(), anyLong())).thenReturn(data);
        validator.validate(request);
    }

    @Test
    public void validate_InactiveLoyaltyCard() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        request.setExchangeLoyaltyCard("abc");
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
        LoyaltyCardData data = new LoyaltyCardData();
        data.setStatus(CardStatus.IN_ACTIVE.code());
        when(loyaltyService.findByCardNumber(anyString(), anyLong())).thenReturn(data);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INACTIVE_LOYALTY_CARD.code(), e.getCode());
        }
    }

    @Test
    public void validate_ActiveLoyaltyCard_ButNotAssigned_ShouldNotAccepted() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        request.setExchangeLoyaltyCard("abc");
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
        LoyaltyCardData data = new LoyaltyCardData();
        data.setStatus(CardStatus.ACTIVE.code());
        when(loyaltyService.findByCardNumber(anyString(), anyLong())).thenReturn(data);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.LOYALTY_CARD_HAS_NOT_ASSIGNED.code(), e.getCode());
        }
    }

    @Test
    public void validate_exchangeCart_HasNotEntries() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");
        cartModel.setEntries(new ArrayList<>());
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CART_HAS_NOT_ENTRIES.code(), e.getCode());
        }
    }

    @Test
    public void validate_exchangeCart_invalidComboId() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");

        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setId(12l);
        entryModel.setProductId(123l);
        entryModel.setComboType(ComboType.ONE_GROUP.toString());
        cartModel.setEntries(Arrays.asList(entryModel));

        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(null);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_COMBO_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_exchangeCart_invalid_sub_order_entry_quantity() {
        request.setCompanyId(1l);
        request.setOriginOrderCode("originOrderCode");
        request.setNote(OrderRequestValidatorTest.NOTE);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(order);
        request.setReturnOrderEntries(returnOrderEntries);
        returnOrderEntryRequest.setEntryNumber(0);
        returnOrderEntryRequest.setOrderEntryId(0l);
        returnOrderEntryRequest.setQuantity(3);
        request.setExchange(true);
        request.setExchangeCartCode("exchangeCartCode");

        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setId(12l);
        entryModel.setProductId(123l);
        entryModel.setQuantity(1L);
        entryModel.setComboType(ComboType.ONE_GROUP.toString());

        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setId(11L);
        subOrderEntryModel.setQuantity(1);
        entryModel.setSubOrderEntries(new HashSet<>(Arrays.asList(subOrderEntryModel)));
        cartModel.setEntries(Arrays.asList(entryModel));

        ComboData comboData = new ComboData();
        comboData.setId(123l);
        comboData.setComboType(ComboType.ONE_GROUP.toString());
        comboData.setTotalItemQuantity(3);

        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cartModel);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY.code(), e.getCode());
        }
    }

}
