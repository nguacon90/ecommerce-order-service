package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.*;
import com.vctek.util.ComboType;
import com.vctek.util.MoneySourceType;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class OrderRequestValidatorTest {
    @Mock
    private CartService cartService;
    @Mock
    private AuthService userService;
    @Mock
    private CartModel cart;

    private OrderRequestValidator validator;
    private OrderRequest orderRequest = new OrderRequest();

    public static final String NOTE = "Tivi phù hợp lắp đặt trong mọi không gian nội " +
            "thất từ phòng khách đến phòng ngủ, từ sử dụng cho " +
            "căn hộ đến lắp đặt cho văn phòng, lớp học, cửa hàng,... " +
            "Ngoài ra, để tối ưu cho không gian lắp đặt, người dùng " +
            "hoàn toàn có thể tháo rời chân đế treo tường, không những " +
            "tiết kiệm diện tích lắp đặt, còn mang đến sự tiện nghi cho ngôi nhà của bạn.";
    @Mock
    private FinanceService financeService;
    @Mock
    private AbstractOrderEntryModel comboEntryMock;
    @Mock
    private ProductService productServiceMock;
    @Mock
    private SubOrderEntryModel subComboEntry1;

    @Mock
    private SubOrderEntryModel subComboEntry2;
    @Mock
    private CustomerRequest customerRequestMock;
    @Mock
    private PaymentTransactionRequest bankPaymentMock1;
    @Mock
    private PaymentTransactionRequest bankPaymentMock2;
    @Mock
    private PaymentTransactionRequest cashPaymentMock;
    @Mock
    private MoneySourceData cashMock;
    @Mock
    private MoneySourceData bankMock;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private CouponService couponService;
    @Mock
    private ValidCouponCodeData validCouponMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new OrderRequestValidator(cartService);
        validator.setFinanceService(financeService);
        validator.setProductService(productServiceMock);
        validator.setLoyaltyService(loyaltyService);
        validator.setCouponService(couponService);
        validator.setAuthService(userService);
        orderRequest.setCompanyId(1l);
        when(userService.getCurrentUserId()).thenReturn(2l);
        when(bankPaymentMock1.getMoneySourceId()).thenReturn(12l);
        when(bankPaymentMock1.getPaymentMethodId()).thenReturn(1l);
        when(bankPaymentMock2.getMoneySourceId()).thenReturn(22l);
        when(bankPaymentMock2.getPaymentMethodId()).thenReturn(2l);
        when(cashPaymentMock.getMoneySourceId()).thenReturn(222l);
        when(financeService.getMoneySource(eq(222l), anyLong())).thenReturn(cashMock);
        when(financeService.getMoneySource(anyLong(), anyLong())).thenReturn(bankMock);
        when(cashMock.getType()).thenReturn(MoneySourceType.CASH.toString());
        when(bankMock.getType()).thenReturn(MoneySourceType.BANK_ACCOUNT.toString());
        when(couponService.getValidatedCouponCode(cart)).thenReturn(validCouponMock);
        when(validCouponMock.isValid()).thenReturn(true);
    }

    @Test
    public void validate_emptyCartId() {
        try {
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_CART_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_NotExistedCart() {
        try {
            orderRequest.setCode("cartCode");
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(null);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_EXISTED_ORDER_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyCartEntries() {
        try {
            orderRequest.setCode("cartCode");
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CART_HAS_NOT_ENTRIES.code(), e.getCode());
        }
    }

    @Test
    public void validate_OnlineOrder_emptyCustomerInfo() {
        try {
            orderRequest.setCode("cartCode");
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.code(), e.getCode());
        }
    }

    @Test
    public void validate_OnlineOrder_CustomerInfo_emptyName() {
        try {
            orderRequest.setCode("cartCode");
            orderRequest.setCustomer(customerRequestMock);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.code(), e.getCode());
        }
    }

    @Test
    public void validate_OnlineOrder_CustomerInfo_emptyPhone() {
        try {
            orderRequest.setCode("cartCode");
            orderRequest.setCustomer(customerRequestMock);
            when(customerRequestMock.getName()).thenReturn("name");
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.code(), e.getCode());
        }
    }

    @Test
    public void validate_NoteOverMaximumChars() {
        try {
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE + NOTE);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOTE_OVER_MAX_LENGTH.code(), e.getCode());
        }
    }

    @Test
    public void retail_paymentAmountSmallerThanFinalPrice() {
        try {
            orderRequest.setOrderType(OrderType.RETAIL.toString());
            orderRequest.setCode("cartCode");
            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock));
            when(bankPaymentMock1.getAmount()).thenReturn(100000d);
            when(cashPaymentMock.getAmount()).thenReturn(0d);
            when(cart.getFinalPrice()).thenReturn(100001d);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PAID_AMOUNT_FOR_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void retail_paymentAmountEqualsFinalPrice() {
        orderRequest.setOrderType(OrderType.RETAIL.toString());
        orderRequest.setCode("cartCode");
        orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock));
        when(bankPaymentMock1.getAmount()).thenReturn(100000d);
        when(cashPaymentMock.getAmount()).thenReturn(500d);
        when(cart.getFinalPrice()).thenReturn(100500d);
        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        validator.validate(orderRequest);
        assertTrue("success", true);
    }

    @Test
    public void retail_paymentAmountLargerThanFinalPrice() {
        orderRequest.setOrderType(OrderType.RETAIL.toString());
        orderRequest.setCode("cartCode");
        orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock));
        when(bankPaymentMock1.getAmount()).thenReturn(100000d);
        when(cashPaymentMock.getAmount()).thenReturn(5000d);
        when(cart.getFinalPrice()).thenReturn(100500d);
        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        validator.validate(orderRequest);
        assertTrue("success", true);
    }

    @Test
    public void testCustomer_EmptyPhone_HasLoyaltyCardNumber() {
        try {
            orderRequest.setOrderType(OrderType.RETAIL.toString());
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE);
            orderRequest.setCustomer(customerRequestMock);
            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock));
            when(customerRequestMock.getPhone()).thenReturn(null);
            orderRequest.setCardNumber("272332623456356");

            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_LOYALTY_PHONE_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void testCustomer_HasPhone_HasInvalidLoyaltyCardNumber() {
        try {
            orderRequest.setOrderType(OrderType.RETAIL.toString());
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE);
            orderRequest.setCustomer(customerRequestMock);
            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock));
            when(customerRequestMock.getPhone()).thenReturn("092828273");
            orderRequest.setCardNumber("272332623456356");
            when(loyaltyService.isValid(any())).thenReturn(false);

            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_LOYALTY_CARD_NUMBER.code(), e.getCode());
        }
    }

    @Test
    public void testCustomer_HasPhone_ValidLoyaltyCardNumber() {
        orderRequest.setOrderType(OrderType.RETAIL.toString());
        orderRequest.setCode("cartCode");
        orderRequest.setNote(NOTE);
        orderRequest.setCustomer(customerRequestMock);
        orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock));
        when(customerRequestMock.getPhone()).thenReturn("092828273");
        orderRequest.setCardNumber("272332623456356");
        when(loyaltyService.isValid(any())).thenReturn(true);

        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        validator.validate(orderRequest);
        assertTrue("success", true);
    }

    @Test
    public void testCustomer_Online() {
        try {
            orderRequest.setOrderType(OrderType.ONLINE.toString());
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.code(), e.getCode());
        }
    }

    @Test
    public void testShipping_Online() {
        try {
            orderRequest.setOrderType(OrderType.ONLINE.toString());
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE);
            orderRequest.setCustomer(customerRequestMock);
            when(customerRequestMock.getPhone()).thenReturn("0909090900");
            when(customerRequestMock.getName()).thenReturn("name");
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_SHIPPING_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void testOnline_EmptyOrderSource() {
        try {
            orderRequest.setOrderType(OrderType.ONLINE.toString());
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE);
            orderRequest.setCustomer(customerRequestMock);
            orderRequest.setShippingCompanyId(2l);
            when(customerRequestMock.getPhone()).thenReturn("0909090900");
            when(customerRequestMock.getName()).thenReturn("name");
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_SOURCE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_success_online() {
        orderRequest.setOrderType(OrderType.ONLINE.toString());
        orderRequest.setCode("cartCode");
        orderRequest.setNote(NOTE);
        orderRequest.setCustomer(customerRequestMock);
        orderRequest.setShippingCompanyId(1l);
        orderRequest.setOrderSourceId(1l);
        when(customerRequestMock.getPhone()).thenReturn("0909090900");
        when(customerRequestMock.getName()).thenReturn("name");
        when(customerRequestMock.getPhone()).thenReturn("0909090900");
        when(customerRequestMock.getName()).thenReturn("name");
        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        validator.validate(orderRequest);
    }


    @Test
    public void validate_cartContainInvalidComboId() {
        try {
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel(), comboEntryMock));
            when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(cartService.isComboEntry(comboEntryMock)).thenReturn(true);
            when(comboEntryMock.getProductId()).thenReturn(222l);
            when(comboEntryMock.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
            when(productServiceMock.getCombo(anyLong(), anyLong())).thenReturn(null);

            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_COMBO_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_cartContainComboWithCorrectQty() {
        try {
            orderRequest.setCode("cartCode");
            orderRequest.setNote(NOTE);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel(), comboEntryMock));
            when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(cartService.isComboEntry(comboEntryMock)).thenReturn(true);
            when(comboEntryMock.getProductId()).thenReturn(222l);
            when(comboEntryMock.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
            when(comboEntryMock.getSubOrderEntries())
                    .thenReturn(new LinkedHashSet(Arrays.asList(subComboEntry1, subComboEntry2)));
            when(subComboEntry1.getQuantity()).thenReturn(1);
            when(subComboEntry1.getQuantity()).thenReturn(1);
            when(subComboEntry1.getQuantity()).thenReturn(1);
            when(subComboEntry2.getQuantity()).thenReturn(1);
            ComboData comboData = new ComboData();
            comboData.setTotalItemQuantity(3);
            when(productServiceMock.getCombo(anyLong(), anyLong())).thenReturn(comboData);

            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY.code(), e.getCode());
        }
    }

    @Test
    public void validate_cartContainCombo_isValid() {
        orderRequest.setOrderType(OrderType.ONLINE.toString());
        orderRequest.setCode("cartCode");
        orderRequest.setNote(NOTE);
        orderRequest.setCustomer(customerRequestMock);
        orderRequest.setShippingCompanyId(1l);
        orderRequest.setOrderSourceId(1l);
        when(customerRequestMock.getPhone()).thenReturn("0909090900");
        when(customerRequestMock.getName()).thenReturn("name");
        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel(), comboEntryMock));
        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(cartService.isComboEntry(comboEntryMock)).thenReturn(true);
        when(comboEntryMock.getProductId()).thenReturn(222l);
        when(comboEntryMock.getQuantity()).thenReturn(2l);
        when(comboEntryMock.getSubOrderEntries())
                .thenReturn(new LinkedHashSet(Arrays.asList(subComboEntry1, subComboEntry2)));
        when(subComboEntry1.getQuantity()).thenReturn(4);
        when(subComboEntry2.getQuantity()).thenReturn(2);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(3);
        when(productServiceMock.getCombo(anyLong(), anyLong())).thenReturn(comboData);

        validator.validate(orderRequest);
    }

    @Test
    public void validate_existedInvalidCouponCode() {
        try {
            orderRequest.setOrderType(OrderType.RETAIL.toString());
            orderRequest.setCode("cartCode");
            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock));
            when(bankPaymentMock1.getAmount()).thenReturn(100000d);
            when(cashPaymentMock.getAmount()).thenReturn(5000d);
            when(cart.getFinalPrice()).thenReturn(100500d);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cart.getType()).thenReturn(OrderType.RETAIL.toString());
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(validCouponMock.isValid()).thenReturn(false);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_INVALID_COUPON.message(), e.getMessage());
        }
    }

    @Test
    public void validate_payment_loyaltyPoint_emptyLoyaltyCardNumber() {
        try {
            PaymentTransactionRequest loyaltyPayment = new PaymentTransactionRequest();
            loyaltyPayment.setMoneySourceId(3l);
            loyaltyPayment.setPaymentMethodId(3l);
            loyaltyPayment.setAmount(10000d);

            PaymentMethodData paymentMethodData = new PaymentMethodData();
            paymentMethodData.setId(3l);

            orderRequest.setCode("cartCode");
            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock, loyaltyPayment));
            when(bankPaymentMock1.getAmount()).thenReturn(10000d);
            when(cashPaymentMock.getAmount()).thenReturn(5000d);
            when(cart.getFinalPrice()).thenReturn(25000d);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_LOYALTY_CARD_NUMBER.message(), e.getMessage());
        }
    }

    @Test
    public void validate_payment_loyaltyPoint_cannotExceedAvailablePoints() {
        try {
            PaymentTransactionRequest loyaltyPayment = new PaymentTransactionRequest();
            loyaltyPayment.setMoneySourceId(3l);
            loyaltyPayment.setPaymentMethodId(3l);
            loyaltyPayment.setAmount(10000d);

            PaymentMethodData paymentMethodData = new PaymentMethodData();
            paymentMethodData.setId(3l);

            AvailablePointAmountData pointAmountData = new AvailablePointAmountData();
            pointAmountData.setAvailableAmount(9d);
            pointAmountData.setConversionRate(1000d);

            orderRequest.setCode("cartCode");
            orderRequest.setCardNumber("cardNumber");
            orderRequest.setCustomer(customerRequestMock);
            when(customerRequestMock.getPhone()).thenReturn("092828273");
            when(loyaltyService.isValid(any())).thenReturn(true);

            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock, loyaltyPayment));
            when(bankPaymentMock1.getAmount()).thenReturn(10000d);
            when(cashPaymentMock.getAmount()).thenReturn(5000d);
            when(cart.getFinalPrice()).thenReturn(25000d);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
            when(loyaltyService.computeAvailablePointAmountOf(any())).thenReturn(pointAmountData);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_EXCEED_AVAILABLE_POINTS.message(), e.getMessage());
        }
    }

    @Test
    public void validate_payment_loyaltyPoint_invalidPointForOrder() {
        try {
            PaymentTransactionRequest loyaltyPayment = new PaymentTransactionRequest();
            loyaltyPayment.setMoneySourceId(3l);
            loyaltyPayment.setPaymentMethodId(3l);
            loyaltyPayment.setAmount(10000d);

            PaymentMethodData paymentMethodData = new PaymentMethodData();
            paymentMethodData.setId(3l);

            AvailablePointAmountData pointAmountData = new AvailablePointAmountData();
            pointAmountData.setAvailableAmount(10d);
            pointAmountData.setPointAmount(9d);
            pointAmountData.setConversionRate(1000d);

            orderRequest.setCode("cartCode");
            orderRequest.setCardNumber("cardNumber");
            orderRequest.setCustomer(customerRequestMock);
            when(customerRequestMock.getPhone()).thenReturn("092828273");
            when(loyaltyService.isValid(any())).thenReturn(true);

            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock, loyaltyPayment));
            when(bankPaymentMock1.getAmount()).thenReturn(10000d);
            when(cashPaymentMock.getAmount()).thenReturn(5000d);
            when(cart.getFinalPrice()).thenReturn(25000d);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
            when(loyaltyService.computeAvailablePointAmountOf(any())).thenReturn(pointAmountData);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_POINT_FOR_ORDER.message(), e.getMessage());
        }
    }

    @Test
    public void validate_payment_loyaltyPoint_invalidPoint_BecauseNotOddNumber() {
        try {
            PaymentTransactionRequest loyaltyPayment = new PaymentTransactionRequest();
            loyaltyPayment.setMoneySourceId(3l);
            loyaltyPayment.setPaymentMethodId(3l);
            loyaltyPayment.setAmount(8.5);

            PaymentMethodData paymentMethodData = new PaymentMethodData();
            paymentMethodData.setId(3l);

            AvailablePointAmountData pointAmountData = new AvailablePointAmountData();
            pointAmountData.setAvailableAmount(10d);
            pointAmountData.setPointAmount(9d);
            pointAmountData.setConversionRate(1000d);

            orderRequest.setCode("cartCode");
            orderRequest.setCardNumber("cardNumber");
            orderRequest.setCustomer(customerRequestMock);
            when(customerRequestMock.getPhone()).thenReturn("092828273");
            when(loyaltyService.isValid(any())).thenReturn(true);

            orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock, loyaltyPayment));
            when(bankPaymentMock1.getAmount()).thenReturn(10000d);
            when(cashPaymentMock.getAmount()).thenReturn(5000d);
            when(cart.getFinalPrice()).thenReturn(25000d);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
            when(loyaltyService.computeAvailablePointAmountOf(any())).thenReturn(pointAmountData);
            validator.validate(orderRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_INTEGER_POINT_FOR_ORDER.message(), e.getMessage());
        }
    }

    @Test
    public void validate_payment_loyaltyPoint_success() {
        PaymentTransactionRequest loyaltyPayment = new PaymentTransactionRequest();
        loyaltyPayment.setMoneySourceId(3l);
        loyaltyPayment.setPaymentMethodId(3l);
        loyaltyPayment.setAmount(10000d);

        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(3l);

        AvailablePointAmountData pointAmountData = new AvailablePointAmountData();
        pointAmountData.setAvailableAmount(10d);
        pointAmountData.setPointAmount(10d);
        pointAmountData.setConversionRate(1000d);

        orderRequest.setCode("cartCode");
        orderRequest.setCardNumber("cardNumber");
        orderRequest.setCustomer(customerRequestMock);
        when(customerRequestMock.getPhone()).thenReturn("092828273");
        when(loyaltyService.isValid(any())).thenReturn(true);

        orderRequest.setPayments(Arrays.asList(bankPaymentMock1, cashPaymentMock, loyaltyPayment));
        when(bankPaymentMock1.getAmount()).thenReturn(10000d);
        when(cashPaymentMock.getAmount()).thenReturn(5000d);
        when(cart.getFinalPrice()).thenReturn(25000d);
        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        when(loyaltyService.computeAvailablePointAmountOf(any())).thenReturn(pointAmountData);
        validator.validate(orderRequest);
    }

    @Test
    public void validateDeliveryDate() {
        try {
            orderRequest.setCode("cartCode");
            orderRequest.setOrderType(OrderType.ONLINE.toString());
            orderRequest.setCustomer(customerRequestMock);
            when(customerRequestMock.getPhone()).thenReturn("092828273");

            when(cart.getFinalPrice()).thenReturn(25000d);
            when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
            when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
            when(userService.isCheckDeliveryDate(anyLong())).thenReturn(true);
            validator.validate(orderRequest);
            fail("throw new exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_DELIVERY_DATE.message(), e.getMessage());
        }
    }

    @Test
    public void validateDeliveryDate_notValid_success() {
        orderRequest.setCode("cartCode");
        orderRequest.setOrderType(OrderType.ONLINE.toString());
        orderRequest.setCustomer(customerRequestMock);
        orderRequest.setShippingCompanyId(2L);
        orderRequest.setOrderSourceId(2L);
        when(customerRequestMock.getPhone()).thenReturn("092828273");

        when(cart.getFinalPrice()).thenReturn(25000d);
        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(userService.isCheckDeliveryDate(anyLong())).thenReturn(false);
        validator.validate(orderRequest);
    }

    @Test
    public void validateDeliveryDate_success() {
        orderRequest.setCode("cartCode");
        orderRequest.setOrderType(OrderType.ONLINE.toString());
        orderRequest.setCustomer(customerRequestMock);
        orderRequest.setShippingCompanyId(2L);
        orderRequest.setDeliveryDate(Calendar.getInstance().getTime());
        orderRequest.setOrderSourceId(2L);
        when(customerRequestMock.getPhone()).thenReturn("092828273");

        when(cart.getFinalPrice()).thenReturn(25000d);
        when(cart.getEntries()).thenReturn(Arrays.asList(new CartEntryModel()));
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);
        when(userService.isCheckDeliveryDate(anyLong())).thenReturn(true);
        validator.validate(orderRequest);
    }
}
