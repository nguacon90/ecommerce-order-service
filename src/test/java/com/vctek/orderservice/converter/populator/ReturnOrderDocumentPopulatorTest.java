package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.BillDetailData;
import com.vctek.orderservice.feignclient.dto.ReturnOrderBillData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.*;
import com.vctek.redis.ProductData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReturnOrderDocumentPopulatorTest {
    private ReturnOrderDocumentPopulator populator;

    @Mock
    private OrderService orderService;
    @Mock
    private AuthService authService;
    @Mock
    private CRMService crmService;
    @Mock
    private ProductService productService;
    @Mock
    private BillService billService;
    @Mock
    private Populator<OrderModel, ExchangeOrder> exchangeOrderPopulator;
    @Mock
    private ReturnOrderModel model;
    private ReturnOrderDocument target = new ReturnOrderDocument();
    @Mock
    private OrderModel originOrder;
    @Mock
    private ReturnOrderBillData data;
    private List<BillDetailData> detailData = new ArrayList<>();
    @Mock
    private BillDetailData detail1;
    @Mock
    private BillDetailData detail2;
    @Mock
    private OrderModel exchangeOrder;
    @Mock
    private Converter<PaymentTransactionModel, PaymentTransactionData> paymentTransactionDataConverter;
    @Mock
    private PaymentTransactionService paymentTransactionService;
    @Mock
    private ProductData productData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new ReturnOrderDocumentPopulator();
        populator.setAuthService(authService);
        populator.setBillService(billService);
        populator.setCrmService(crmService);
        populator.setExchangeOrderPopulator(exchangeOrderPopulator);
        populator.setOrderService(orderService);
        populator.setProductService(productService);
        populator.setReturnOrderPaymentTransactionConverter(paymentTransactionDataConverter);
        populator.setPaymentTransactionService(paymentTransactionService);
        detailData.add(detail1);
        detailData.add(detail2);
    }

    @Test
    public void populate_hasNotOriginOrder() {
        try {
            when(model.getOriginOrder()).thenReturn(null);
            populator.populate(model, target);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.RETURN_ORDER_HAS_NOT_ORIGIN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void populate_notCombo() {
        when(model.getOriginOrder()).thenReturn(originOrder);
        when(originOrder.getId()).thenReturn(11l);
        when(orderService.findById(anyLong())).thenReturn(originOrder);
        when(originOrder.getCreateByUser()).thenReturn(1l);
        when(originOrder.getCustomerId()).thenReturn(22l);
        when(model.getId()).thenReturn(11l);
        when(originOrder.getCompanyId()).thenReturn(1l);
        when(model.getBillId()).thenReturn(222l);
        when(data.getEntries()).thenReturn(detailData);
        when(detail1.getDiscount()).thenReturn(10000d);
        when(detail1.getQuantity()).thenReturn(10);
        when(detail1.getPrice()).thenReturn(23000d);
        when(detail1.getComboId()).thenReturn(null);

        when(detail2.getDiscount()).thenReturn(0d);
        when(detail2.getQuantity()).thenReturn(3);
        when(detail2.getPrice()).thenReturn(25000d);
        when(detail2.getComboId()).thenReturn(null);
        when(billService.getReturnOrderBill(anyLong(), anyLong(), anyLong()))
                .thenReturn(data);
        when(model.getExchangeOrder()).thenReturn(exchangeOrder);
        when(productService.getBasicProductDetail(anyLong())).thenReturn(productData);

        populator.populate(model, target);
        verify(authService).getUserById(anyLong());
        verify(crmService).getCustomer(anyLong(), anyLong());
        verify(exchangeOrderPopulator).populate(eq(exchangeOrder), any(ExchangeOrder.class));
        double expectedFinalBillPrice = calculateFinalPrice(detail1) + calculateFinalPrice(detail2);
        assertEquals(expectedFinalBillPrice, target.getBill().getFinalPrice(), 0);
    }

    @Test
    public void populate_widthComboId() {
        when(model.getOriginOrder()).thenReturn(originOrder);
        when(originOrder.getId()).thenReturn(11l);
        when(orderService.findById(anyLong())).thenReturn(originOrder);
        when(originOrder.getCreateByUser()).thenReturn(1l);
        when(originOrder.getCustomerId()).thenReturn(22l);
        when(model.getId()).thenReturn(11l);
        when(originOrder.getCompanyId()).thenReturn(1l);
        when(model.getBillId()).thenReturn(222l);
        when(data.getEntries()).thenReturn(detailData);
        when(detail1.getDiscount()).thenReturn(10000d);
        when(detail1.getQuantity()).thenReturn(10);
        when(detail1.getPrice()).thenReturn(23000d);

        when(detail2.getDiscount()).thenReturn(0d);
        when(detail2.getQuantity()).thenReturn(3);
        when(detail2.getPrice()).thenReturn(25000d);
        when(billService.getReturnOrderBill(anyLong(), anyLong(), anyLong()))
                .thenReturn(data);
        when(model.getExchangeOrder()).thenReturn(exchangeOrder);
        when(productService.getBasicProductDetail(anyLong())).thenReturn(productData, productData);

        populator.populate(model, target);
        verify(authService).getUserById(anyLong());
        verify(crmService).getCustomer(anyLong(), anyLong());
        verify(exchangeOrderPopulator).populate(eq(exchangeOrder), any(ExchangeOrder.class));
        double expectedFinalBillPrice = calculateFinalPrice(detail1) + calculateFinalPrice(detail2);
        assertEquals(expectedFinalBillPrice, target.getBill().getFinalPrice(), 0);
    }

    private double calculateFinalPrice(BillDetailData detail1) {
        return detail1.getPrice() * detail1.getQuantity() - detail1.getDiscount();
    }
}
