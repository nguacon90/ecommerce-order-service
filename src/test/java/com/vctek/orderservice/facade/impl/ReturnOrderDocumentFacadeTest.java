package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.dto.OrderEntryExcelData;
import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderBill;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderEntry;
import com.vctek.orderservice.elasticsearch.service.ReturnOrderDocumentService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.BillClient;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReturnOrderDocumentFacadeTest {
    private ReturnOrderDocumentFacadeImpl facade;

    @Mock
    private ReturnOrderService returnOrderService;
    @Mock
    private ReturnOrderDocumentService returnOrderDocumentService;
    @Mock
    private Converter<ReturnOrderModel, ReturnOrderDocument> returnOrderDocumentConverter;
    @Mock
    private Populator<OrderModel, ExchangeOrder> exchangeOrderDocumentPopulator;
    private List<ReturnOrderModel> content = new ArrayList<>();
    @Mock
    private ReturnOrderModel returnOrderModel;
    @Mock
    private ReturnOrderDocument returnOrderDoc;
    @Mock
    private OrderModel exchangeOrder;
    @Mock
    private ReturnOrderSearchRequest searchRequest;
    @Mock
    private BillClient billClient;

    private ArgumentCaptor<NativeSearchQuery> captor;
    @Mock
    private OrderData orderDataMock;
    @Mock
    private OrderService orderServiceMock;
    private OrderModel orderModel = new OrderModel();
    private ReturnOrderDocument document;
    private Long companyId = 1l;


    @Before
    public void setUp() {
        content.add(returnOrderModel);
        document = new ReturnOrderDocument();
        MockitoAnnotations.initMocks(this);
        facade = new ReturnOrderDocumentFacadeImpl();
        facade.setBulkSize(100);
        facade.setNumberOfThread(1);
        facade.setExchangeOrderDocumentPopulator(exchangeOrderDocumentPopulator);
        facade.setReturnOrderDocumentConverter(returnOrderDocumentConverter);
        facade.setReturnOrderDocumentService(returnOrderDocumentService);
        facade.setReturnOrderService(returnOrderService);
        facade.setBillClient(billClient);
        facade.setOrderService(orderServiceMock);
        captor = ArgumentCaptor.forClass(NativeSearchQuery.class);
    }

    @Test
    public void fullIndex() throws InterruptedException {
        when(returnOrderDocumentService.existedIndex()).thenReturn(true);
        when(returnOrderService.findAllByCompanyId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 100), 1), Page.empty());
        when(returnOrderDocumentConverter.convert(returnOrderModel)).thenReturn(returnOrderDoc);

        facade.partialIndex(companyId);
        Thread.sleep(200);
        verify(returnOrderDocumentService).bulkIndex(anyList());
    }

    @Test
    public void index() {
        when(returnOrderDocumentConverter.convert(returnOrderModel)).thenReturn(returnOrderDoc);

        facade.index(returnOrderModel);
        verify(returnOrderDocumentService).save(returnOrderDoc);
    }

    @Test
    public void updateExchangeOrder_indexNew() {
        when(returnOrderDocumentService.findById(anyLong())).thenReturn(null);
        when(returnOrderDocumentConverter.convert(returnOrderModel)).thenReturn(returnOrderDoc);

        facade.updateExchangeOrder(exchangeOrder, returnOrderModel);
        verify(returnOrderDocumentService).save(returnOrderDoc);
    }

    @Test
    public void updateExchangeOrder_updateExchangeOrder() {
        when(returnOrderDocumentService.findById(anyLong())).thenReturn(returnOrderDoc);
        when(returnOrderModel.getId()).thenReturn(1l);

        facade.updateExchangeOrder(exchangeOrder, returnOrderModel);
        verify(exchangeOrderDocumentPopulator).populate(eq(exchangeOrder), any(ExchangeOrder.class));
        verify(returnOrderDocumentService, times(0)).save(returnOrderDoc);
        verify(returnOrderDocumentService).updateExchangeOrder(anyLong(), any(ExchangeOrder.class));
    }

    @Test
    public void search_emptyCompanyId() {
        try {
            when(searchRequest.getCompanyId()).thenReturn(null);
            facade.search(searchRequest, PageRequest.of(0, 20));
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void searchInvalidPeymentMethods() {
        when(searchRequest.getCompanyId()).thenReturn(1l);
        when(searchRequest.getProduct()).thenReturn("product");
        when(searchRequest.getPaymentMethods()).thenReturn("a");
       try {
           facade.search(searchRequest, PageRequest.of(0, 20));
           fail("Must throws exception");
        }catch (ServiceException e){
           assertEquals(ErrorCodes.INVALID_PAYMENT_METHOD_ID.code(), e.getCode());
       }
    }

    @Test
    public void searchSuccess(){
        when(searchRequest.getCompanyId()).thenReturn(1L);
        when(searchRequest.getProduct()).thenReturn("product");
        when(searchRequest.getPaymentMethods()).thenReturn("-1,1,2");
        facade.search(searchRequest, PageRequest.of(0,20));
        verify(returnOrderDocumentService).search(any(SearchQuery.class));
    }



    @Test
    public void exportExcelOrder() {
        when(searchRequest.getCompanyId()).thenReturn(1L);

        ReturnOrderDocument orderDocument = new ReturnOrderDocument();
        orderDocument.setId(12l);
        orderDocument.setCreationTime(Calendar.getInstance().getTime());
        orderDocument.setCompanyId(1l);
        orderDocument.setExchangeWarehouseId(2l);
        orderDocument.setReturnWarehouseId(2l);

        ReturnOrderEntry returnOrderEntry = new ReturnOrderEntry();
        returnOrderEntry.setProductId(12l);
        returnOrderEntry.setProductName("product");
        returnOrderEntry.setProductSku("sku");

        ReturnOrderBill bill = new ReturnOrderBill();
        bill.setFinalPrice(1000d);
        bill.setCompanyId(1l);
        bill.setEntries(Arrays.asList(returnOrderEntry));

        orderDocument.setBill(bill);

        OrderEntryExcelData orderEntryExcelData = new OrderEntryExcelData();
        orderEntryExcelData.setId("112233");
        orderEntryExcelData.setName("112233");

        OrderSearchExcelData orderSearchExcelData = new OrderSearchExcelData();
        orderSearchExcelData.setOrderEntryExcelData(Arrays.asList(orderEntryExcelData));

        Page<ReturnOrderDocument> pages = new PageImpl<>(Arrays.asList(orderDocument));
        when(returnOrderDocumentService.search(any())).thenReturn(pages, Page.empty());
        when(billClient.getWarehouseByCompany(any())).thenReturn(Arrays.asList(new WarehouseData()));
        OrderSearchExcelData data = facade.exportExcelListReturnOrder(searchRequest);
        assertEquals(1, data.getOrderEntryExcelData().size());
    }

    @Test
    public void indexOrderSource_NotOnlineOrder_ShouldIgnore() {
        when(orderDataMock.getOrderType()).thenReturn(OrderType.RETAIL.name());
        facade.indexOrderSource(orderDataMock);
        verify(returnOrderDocumentService, times(0)).saveAll(anyList());
    }

    @Test
    public void indexOrderSource_ExchangeOrder_ShouldIgnore() {
        when(orderDataMock.isExchange()).thenReturn(true);
        when(orderDataMock.getOrderType()).thenReturn(OrderType.ONLINE.name());
        facade.indexOrderSource(orderDataMock);
        verify(returnOrderDocumentService, times(0)).saveAll(anyList());
    }

    @Test
    public void indexOrderSource_NotFoundOrder_ShouldIgnore() {
        when(orderDataMock.isExchange()).thenReturn(false);
        when(orderDataMock.getOrderType()).thenReturn(OrderType.ONLINE.name());
        when(orderDataMock.getOrderCode()).thenReturn("orderCode");
        when(orderDataMock.getCompanyId()).thenReturn(1l);
        when(orderServiceMock.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
        facade.indexOrderSource(orderDataMock);
        verify(returnOrderDocumentService, times(0)).saveAll(anyList());
    }

    @Test
    public void indexOrderSource_NotFoundReturnOrders_ShouldIgnore() {
        when(orderDataMock.isExchange()).thenReturn(false);
        when(orderDataMock.getOrderType()).thenReturn(OrderType.ONLINE.name());
        when(orderDataMock.getOrderCode()).thenReturn("orderCode");
        when(orderDataMock.getCompanyId()).thenReturn(1l);
        when(orderServiceMock.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        when(returnOrderService.findAllByOriginOrder(orderModel)).thenReturn(Collections.emptyList());
        facade.indexOrderSource(orderDataMock);
        verify(returnOrderDocumentService, times(0)).saveAll(anyList());
    }

    @Test
    public void indexOrderSource_FoundOneReturnOrders() {
        when(orderDataMock.isExchange()).thenReturn(false);
        when(orderDataMock.getOrderType()).thenReturn(OrderType.ONLINE.name());
        when(orderDataMock.getOrderCode()).thenReturn("orderCode");
        when(orderDataMock.getCompanyId()).thenReturn(1l);
        when(orderServiceMock.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        when(returnOrderService.findAllByOriginOrder(orderModel)).thenReturn(Arrays.asList(returnOrderModel));
        when(orderDataMock.getOrderSourceId()).thenReturn(22l);
        when(returnOrderModel.getId()).thenReturn(11l);
        when(returnOrderDocumentService.findById(anyLong())).thenReturn(document);
        facade.indexOrderSource(orderDataMock);
        verify(returnOrderDocumentService, times(1)).saveAll(anyList());
        assertEquals(22l, document.getOriginOrderSourceId(), 0);
    }
}
