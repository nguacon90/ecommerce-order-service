package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CheckOutOfStockParam;
import com.vctek.orderservice.dto.HoldingData;
import com.vctek.orderservice.dto.request.HoldingProductRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.InventoryClient;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.feignclient.dto.UpdateInventoryStatusRequest;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryDetailData;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryRequest;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.util.InventoryStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InventoryServiceTest {
    private InventoryServiceImpl service;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private OrderModel orderMock;
    @Mock
    private OrderEntryModel entryMock1;
    @Mock
    private OrderEntryModel entryMock2;
    private ArgumentCaptor<UpdateProductInventoryRequest> holdingStockCaptor;
    private ArgumentCaptor<UpdateInventoryStatusRequest> preOrderCaptor;
    private HoldingProductRequest holdingProductRequest;
    private HoldingData holdingDataEntry1;
    private HoldingData holdingDataEntry2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        holdingStockCaptor = ArgumentCaptor.forClass(UpdateProductInventoryRequest.class);
        preOrderCaptor = ArgumentCaptor.forClass(UpdateInventoryStatusRequest.class);
        service = new InventoryServiceImpl();
        service.setInventoryClient(inventoryClient);
        when(orderMock.getEntries()).thenReturn(Arrays.asList(entryMock1, entryMock2));
        when(orderMock.getCompanyId()).thenReturn(1l);
        when(orderMock.getWarehouseId()).thenReturn(2l);
        when(entryMock1.getProductId()).thenReturn(222l);
        when(entryMock2.getProductId()).thenReturn(223l);
        holdingProductRequest = new HoldingProductRequest();
        holdingDataEntry1 = new HoldingData();
        holdingDataEntry1.setProductId(222l);
        holdingDataEntry2 = new HoldingData();
        holdingDataEntry2.setProductId(223l);
        holdingProductRequest.setCompanyId(1l);
        holdingProductRequest.setOrderCode("orderCode");
        holdingProductRequest.setHoldingDataList(Arrays.asList(holdingDataEntry1, holdingDataEntry2));
    }

    @Test
    public void getAvailableStock() {
        service.getAvailableStock(1l, 1l, 1l);
        verify(inventoryClient).getAvailableStock(1l, 1l, 1l);
    }

    @Test
    public void holdingAllQuantityOfOrder_HasNotHoldingEntries() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock2.isHolding()).thenReturn(false);

        service.holdingAllQuantityOf(orderMock);
        verify(inventoryClient, times(0))
                .changeInventoryByStatus(any(UpdateProductInventoryRequest.class));
    }

    @Test
    public void holdingAllQuantityOfOrder_HasOneHoldingEntryBefore() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(false);
        when(entryMock2.getQuantity()).thenReturn(20l);

        service.holdingAllQuantityOf(orderMock);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        UpdateProductInventoryRequest request = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.AVAILABLE.code(), request.getFrom());
        assertEquals(InventoryStatus.HOLDING.code(), request.getTo());
        List<UpdateProductInventoryDetailData> detailDataList = request.getDetailDataList();
        assertEquals(1, detailDataList.size());
        UpdateProductInventoryDetailData detailData = detailDataList.get(0);
        assertEquals(20, detailData.getValue(), 0);
    }

    @Test
    public void holdingAllQuantityOfOrder_Has2HoldingEntriesBefore() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(true);
        when(entryMock2.getHoldingStock()).thenReturn(20l);
        when(entryMock2.getQuantity()).thenReturn(20l);

        service.holdingAllQuantityOf(orderMock);
        verify(inventoryClient, times(0)).changeInventoryByStatus(any());
    }

    @Test
    public void changeAllHoldingToAvailableOfOrder_AllEntriesNotHolding() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(false);
        when(entryMock2.getQuantity()).thenReturn(20l);

        service.changeAllHoldingToAvailableOf(orderMock);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        UpdateProductInventoryRequest request = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.HOLDING.code(), request.getFrom());
        assertEquals(InventoryStatus.AVAILABLE.code(), request.getTo());
        assertEquals(2, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        UpdateProductInventoryDetailData detailDataEntry2 = request.getDetailDataList().get(1);
        assertEquals(10, detailDataEntry1.getValue(), 0);
        assertEquals(20, detailDataEntry2.getValue(), 0);
    }

    @Test
    public void changeAllHoldingToAvailableOfOrder_HasOneEntryHolding() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10L);
        when(entryMock1.getQuantity()).thenReturn(10L);

        when(entryMock2.isHolding()).thenReturn(false);
        when(entryMock2.getQuantity()).thenReturn(20L);

        service.changeAllHoldingToAvailableOf(orderMock);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        UpdateProductInventoryRequest request = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.HOLDING.code(), request.getFrom());
        assertEquals(InventoryStatus.AVAILABLE.code(), request.getTo());
        assertEquals(2, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        UpdateProductInventoryDetailData detailDataEntry2 = request.getDetailDataList().get(1);
        assertEquals(10, detailDataEntry1.getValue(), 0);
        assertEquals(20, detailDataEntry2.getValue(), 0);
    }

    @Test
    public void subtractPreOrder_HasNotPreOrderEntry() {
        when(entryMock1.isPreOrder()).thenReturn(false);
        when(entryMock2.isPreOrder()).thenReturn(false);

        service.subtractPreOrder(orderMock);
        verify(inventoryClient, times(0)).subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()),
                any(UpdateInventoryStatusRequest.class));
    }

    @Test
    public void subtractPreOrder_HasOnePreOrderEntry() {
        when(entryMock1.isPreOrder()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isPreOrder()).thenReturn(false);

        service.subtractPreOrder(orderMock);
        verify(inventoryClient, times(1)).subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()),
                preOrderCaptor.capture());
        UpdateInventoryStatusRequest request = preOrderCaptor.getValue();
        assertEquals(1, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        assertEquals(10l, detailDataEntry1.getValue(), 0);
    }

    @Test
    public void subtractPreOrder_Has2PreOrderEntries() {
        when(entryMock1.isPreOrder()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isPreOrder()).thenReturn(true);
        when(entryMock2.getHoldingStock()).thenReturn(20l);
        when(entryMock2.getQuantity()).thenReturn(20l);

        service.subtractPreOrder(orderMock);
        verify(inventoryClient, times(1)).subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()),
                preOrderCaptor.capture());
        UpdateInventoryStatusRequest request = preOrderCaptor.getValue();
        assertEquals(2, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        UpdateProductInventoryDetailData detailDataEntry2 = request.getDetailDataList().get(1);
        assertEquals(10l, detailDataEntry1.getValue(), 0);
        assertEquals(20l, detailDataEntry2.getValue(), 0);
    }

    @Test
    public void resetHoldingStockOfOrder_HasNotHoldingEntry() {
        when(entryMock1.isPreOrder()).thenReturn(false);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isPreOrder()).thenReturn(false);
        when(entryMock2.getQuantity()).thenReturn(20l);

        service.resetHoldingStockOf(orderMock);
        verify(inventoryClient, times(0)).changeInventoryByStatus(any());
    }

    @Test
    public void resetHoldingStockOfOrder_HasOneHoldingEntry() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(false);
        when(entryMock2.getQuantity()).thenReturn(20l);

        service.resetHoldingStockOf(orderMock);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        UpdateProductInventoryRequest request = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.HOLDING.code(), request.getFrom());
        assertEquals(InventoryStatus.AVAILABLE.code(), request.getTo());
        assertEquals(1, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);
    }

    @Test
    public void resetHoldingStockOfOrder_AllHoldingEntries() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(true);
        when(entryMock2.getHoldingStock()).thenReturn(20l);
        when(entryMock2.getQuantity()).thenReturn(20l);

        service.resetHoldingStockOf(orderMock);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        UpdateProductInventoryRequest request = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.HOLDING.code(), request.getFrom());
        assertEquals(InventoryStatus.AVAILABLE.code(), request.getTo());
        assertEquals(2, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);
        UpdateProductInventoryDetailData detailDataEntry2 = request.getDetailDataList().get(1);
        assertEquals(20, detailDataEntry2.getValue(), 0);
    }

    @Test
    public void holdingProducts_EmptyRequest() {
        holdingProductRequest.setHoldingDataList(Collections.emptyList());

        service.holdingProducts(holdingProductRequest, orderMock);
        verify(inventoryClient, times(0)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(0))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), any());
    }

    @Test
    public void holdingProducts_onlyHoldingNotPreOrder() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock1.getHoldingStock()).thenReturn(0l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(false);
        when(entryMock2.getHoldingStock()).thenReturn(0l);
        when(entryMock2.getQuantity()).thenReturn(20l);
        holdingDataEntry1.setHolding(true);
        holdingDataEntry1.setQuantity(10l);
        holdingDataEntry2.setHolding(true);
        holdingDataEntry2.setQuantity(20l);

        service.holdingProducts(holdingProductRequest, orderMock);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(0))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), any());
        verify(entryMock1).setHolding(true);
        verify(entryMock1).setPreOrder(false);
        verify(entryMock1).setHoldingStock(10l);

        verify(entryMock2).setHolding(true);
        verify(entryMock2).setPreOrder(false);
        verify(entryMock2).setHoldingStock(20l);

        UpdateProductInventoryRequest request = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.AVAILABLE.code(), request.getFrom());
        assertEquals(InventoryStatus.HOLDING.code(), request.getTo());
        assertEquals(2, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);
        UpdateProductInventoryDetailData detailDataEntry2 = request.getDetailDataList().get(1);
        assertEquals(20, detailDataEntry2.getValue(), 0);
    }

    @Test
    public void holdingProducts_onlyPreOrderNotHolding() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock1.isPreOrder()).thenReturn(false);
        when(entryMock1.getHoldingStock()).thenReturn(0l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(false);
        when(entryMock2.isPreOrder()).thenReturn(false);
        when(entryMock2.getHoldingStock()).thenReturn(0l);
        when(entryMock2.getQuantity()).thenReturn(20l);
        holdingDataEntry1.setHolding(false);
        holdingDataEntry1.setPreOrder(true);
        holdingDataEntry1.setQuantity(10l);
        holdingDataEntry2.setHolding(false);
        holdingDataEntry2.setPreOrder(true);
        holdingDataEntry2.setQuantity(20l);

        service.holdingProducts(holdingProductRequest, orderMock);
        verify(inventoryClient, times(0)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(1))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(entryMock1).setHolding(false);
        verify(entryMock1).setPreOrder(true);
        verify(entryMock1).setHoldingStock(10l);

        verify(entryMock2).setHolding(false);
        verify(entryMock2).setPreOrder(true);
        verify(entryMock2).setHoldingStock(20l);

        UpdateInventoryStatusRequest request = preOrderCaptor.getValue();
        assertEquals(2, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = request.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);
        UpdateProductInventoryDetailData detailDataEntry2 = request.getDetailDataList().get(1);
        assertEquals(20, detailDataEntry2.getValue(), 0);
    }

    @Test
    public void holdingProducts_holdingAndPreOrder() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock1.isPreOrder()).thenReturn(false);
        when(entryMock1.getHoldingStock()).thenReturn(0l);
        when(entryMock1.getQuantity()).thenReturn(10l);

        when(entryMock2.isHolding()).thenReturn(false);
        when(entryMock2.isPreOrder()).thenReturn(false);
        when(entryMock2.getHoldingStock()).thenReturn(0l);
        when(entryMock2.getQuantity()).thenReturn(20l);
        holdingDataEntry1.setHolding(true);
        holdingDataEntry1.setPreOrder(false);
        holdingDataEntry1.setQuantity(10l);
        holdingDataEntry2.setHolding(false);
        holdingDataEntry2.setPreOrder(true);
        holdingDataEntry2.setQuantity(20l);

        service.holdingProducts(holdingProductRequest, orderMock);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(1))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(entryMock1).setHolding(true);
        verify(entryMock1).setPreOrder(false);
        verify(entryMock1).setHoldingStock(10l);

        verify(entryMock2).setHolding(false);
        verify(entryMock2).setPreOrder(true);
        verify(entryMock2).setHoldingStock(20l);
        UpdateProductInventoryRequest updateProductInventoryRequest = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.AVAILABLE.code(), updateProductInventoryRequest.getFrom());
        assertEquals(InventoryStatus.HOLDING.code(), updateProductInventoryRequest.getTo());
        assertEquals(1, updateProductInventoryRequest.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = updateProductInventoryRequest.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);

        UpdateInventoryStatusRequest request = preOrderCaptor.getValue();
        assertEquals(1, request.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry2 = request.getDetailDataList().get(0);
        assertEquals(20, detailDataEntry2.getValue(), 0);
    }

    @Test
    public void updateHoldingProductOfEntry_FromNotHoldingToHoling() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock1.isPreOrder()).thenReturn(false);
        when(entryMock1.getHoldingStock()).thenReturn(0l);
        when(entryMock1.getQuantity()).thenReturn(10l);
        holdingDataEntry1.setPreOrder(false);
        holdingDataEntry1.setHolding(true);
        holdingDataEntry1.setQuantity(10l);

        service.updateHoldingProductOf(orderMock, entryMock1, holdingDataEntry1);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(0))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), any());
        verify(inventoryClient, times(0))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), any());
        verify(entryMock1).setHolding(true);
        verify(entryMock1, times(0)).setPreOrder(anyBoolean());
        verify(entryMock1).setHoldingStock(10l);

        UpdateProductInventoryRequest updateProductInventoryRequest = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.AVAILABLE.code(), updateProductInventoryRequest.getFrom());
        assertEquals(InventoryStatus.HOLDING.code(), updateProductInventoryRequest.getTo());
        assertEquals(1, updateProductInventoryRequest.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = updateProductInventoryRequest.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);

    }

    @Test
    public void updateHoldingProductOfEntry_FromHoldingToNotHoling() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.isPreOrder()).thenReturn(false);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);
        holdingDataEntry1.setPreOrder(false);
        holdingDataEntry1.setHolding(false);

        service.updateHoldingProductOf(orderMock, entryMock1, holdingDataEntry1);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(0))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), any());
        verify(inventoryClient, times(0))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), any());
        verify(entryMock1).setHolding(false);
        verify(entryMock1, times(0)).setPreOrder(anyBoolean());
        verify(entryMock1).setHoldingStock(0l);

        UpdateProductInventoryRequest updateProductInventoryRequest = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.HOLDING.code(), updateProductInventoryRequest.getFrom());
        assertEquals(InventoryStatus.AVAILABLE.code(), updateProductInventoryRequest.getTo());
        assertEquals(1, updateProductInventoryRequest.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = updateProductInventoryRequest.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);

    }


    @Test
    public void updateHoldingProductOfEntry_FromNotPreOrderToPreOrder() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock1.isPreOrder()).thenReturn(false);
        when(entryMock1.getHoldingStock()).thenReturn(0l);
        when(entryMock1.getQuantity()).thenReturn(10l);
        holdingDataEntry1.setPreOrder(true);
        holdingDataEntry1.setHolding(false);
        holdingDataEntry1.setQuantity(10l);

        service.updateHoldingProductOf(orderMock, entryMock1, holdingDataEntry1);
        verify(inventoryClient, times(0)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(0))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(inventoryClient, times(1))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(entryMock1).setPreOrder(true);
        verify(entryMock1, times(0)).setHolding(anyBoolean());
        verify(entryMock1).setHoldingStock(10l);

        UpdateInventoryStatusRequest inventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = inventoryStatusRequest.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);

    }

    @Test
    public void updateHoldingProductOfEntry_FromPreOrderToNotPreOrder() {
        when(entryMock1.isHolding()).thenReturn(false);
        when(entryMock1.isPreOrder()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(10l);
        holdingDataEntry1.setPreOrder(false);
        holdingDataEntry1.setHolding(false);
        holdingDataEntry1.setQuantity(10l);

        service.updateHoldingProductOf(orderMock, entryMock1, holdingDataEntry1);
        verify(inventoryClient, times(0)).changeInventoryByStatus(holdingStockCaptor.capture());
        verify(inventoryClient, times(1))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(inventoryClient, times(0))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(entryMock1).setPreOrder(false);
        verify(entryMock1, times(0)).setHolding(anyBoolean());
        verify(entryMock1).setHoldingStock(0l);

        UpdateInventoryStatusRequest inventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = inventoryStatusRequest.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);
    }

    @Test
    public void resetHoldingStockOfOrderEntry_NotHolingEntry() {
        when(entryMock1.isHolding()).thenReturn(false);
        service.resetHoldingStockOf(orderMock, entryMock1);
        verify(inventoryClient, times(0)).changeInventoryByStatus(holdingStockCaptor.capture());
    }

    @Test
    public void resetHoldingStockOfOrderEntry_HolingEntry() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        service.resetHoldingStockOf(orderMock, entryMock1);
        verify(inventoryClient, times(1))
                .changeInventoryByStatus(holdingStockCaptor.capture());
        UpdateProductInventoryRequest updateProductInventoryRequest = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.HOLDING.code(), updateProductInventoryRequest.getFrom());
        assertEquals(InventoryStatus.AVAILABLE.code(), updateProductInventoryRequest.getTo());
        assertEquals(1, updateProductInventoryRequest.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = updateProductInventoryRequest.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);

    }

    @Test
    public void subtractPreOrderOfOrderEntry_NotPreOrderEntry() {
        when(entryMock1.isPreOrder()).thenReturn(false);

        service.subtractPreOrder(orderMock, entryMock1);
        verify(inventoryClient, times(0))
                .subtractStockWithInventoryStatus(anyString(), any());
    }

    @Test
    public void subtractPreOrderOfOrderEntry_PreOrderEntry() {
        when(entryMock1.isPreOrder()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);

        service.subtractPreOrder(orderMock, entryMock1);
        verify(inventoryClient, times(1))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        UpdateInventoryStatusRequest updateInventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, updateInventoryStatusRequest.getDetailDataList().size());
        UpdateProductInventoryDetailData detailDataEntry1 = updateInventoryStatusRequest.getDetailDataList().get(0);
        assertEquals(10, detailDataEntry1.getValue(), 0);

    }

    @Test
    public void updateHoldingStockOfOrderEntry_NotHolding() {
        when(entryMock1.isHolding()).thenReturn(false);

        service.updateHoldingStockOf(orderMock, entryMock1);
        verify(inventoryClient, times(0)).changeInventoryByStatus(any());
    }

    @Test
    public void updateHoldingStockOfOrderEntry_SubtractHolding() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(5l);

        service.updateHoldingStockOf(orderMock, entryMock1);
        verify(inventoryClient, times(1))
                .changeInventoryByStatus(holdingStockCaptor.capture());
        verify(entryMock1).setHoldingStock(5l);
        UpdateProductInventoryRequest productInventoryRequest = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.HOLDING.code(), productInventoryRequest.getFrom());
        assertEquals(InventoryStatus.AVAILABLE.code(), productInventoryRequest.getTo());
        assertEquals(1, productInventoryRequest.getDetailDataList().size());
        assertEquals(5, productInventoryRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updateHoldingStockOfOrderEntry_AddHolding() {
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(20l);

        service.updateHoldingStockOf(orderMock, entryMock1);
        verify(inventoryClient, times(1))
                .changeInventoryByStatus(holdingStockCaptor.capture());
        verify(entryMock1).setHoldingStock(20l);
        UpdateProductInventoryRequest productInventoryRequest = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.AVAILABLE.code(), productInventoryRequest.getFrom());
        assertEquals(InventoryStatus.HOLDING.code(), productInventoryRequest.getTo());
        assertEquals(1, productInventoryRequest.getDetailDataList().size());
        assertEquals(10, productInventoryRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updateHoldingStockOfOrderEntry_AddHolding_widthCombo() {
        SubOrderEntryModel subEntry = new SubOrderEntryModel();
        subEntry.setProductId(123l);
        subEntry.setQuantity(60);
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        subOrderEntryModels.add(subEntry);
        when(entryMock1.isHolding()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(20l);
        when(entryMock1.getSubOrderEntries()).thenReturn(subOrderEntryModels);

        service.updateHoldingStockOf(orderMock, entryMock1);
        verify(inventoryClient, times(1))
                .changeInventoryByStatus(holdingStockCaptor.capture());
        verify(entryMock1).setHoldingStock(20l);
        UpdateProductInventoryRequest productInventoryRequest = holdingStockCaptor.getValue();
        assertEquals(InventoryStatus.AVAILABLE.code(), productInventoryRequest.getFrom());
        assertEquals(InventoryStatus.HOLDING.code(), productInventoryRequest.getTo());
        assertEquals(1, productInventoryRequest.getDetailDataList().size());
        assertEquals(30, productInventoryRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updatePreOrderOfOfOrderEntry_NotPreOrder() {
        when(entryMock1.isPreOrder()).thenReturn(false);

        service.updatePreOrderOf(orderMock, entryMock1);
        verify(inventoryClient, times(0)).addStockWithInventoryStatus(anyString(), any());
        verify(inventoryClient, times(0)).subtractStockWithInventoryStatus(anyString(), any());
    }

    @Test
    public void updatePreOrderOfOfOrderEntry_SubtractPreOrder() {
        when(entryMock1.isPreOrder()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(5l);

        service.updatePreOrderOf(orderMock, entryMock1);
        verify(inventoryClient, times(1))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(inventoryClient, times(0))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(entryMock1).setHoldingStock(5l);
        UpdateInventoryStatusRequest inventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        assertEquals(5, inventoryStatusRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updatePreOrderOfOfOrderEntry_AddPreOrder() {
        when(entryMock1.isPreOrder()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(10l);
        when(entryMock1.getQuantity()).thenReturn(25l);

        service.updatePreOrderOf(orderMock, entryMock1);
        verify(inventoryClient, times(0))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(inventoryClient, times(1))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(entryMock1).setHoldingStock(25l);
        UpdateInventoryStatusRequest inventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        assertEquals(15, inventoryStatusRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updatePreOrderOfOfOrderEntry_SubtractPreOrder_widthCombo() {
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        SubOrderEntryModel subEntry = new SubOrderEntryModel();
        subEntry.setQuantity(10);
        subEntry.setProductId(12l);
        subOrderEntryModels.add(subEntry);

        when(entryMock1.isPreOrder()).thenReturn(true);
        when(entryMock1.getHoldingStock()).thenReturn(20l);
        when(entryMock1.getQuantity()).thenReturn(10l);
        when(entryMock1.getSubOrderEntries()).thenReturn(subOrderEntryModels);

        service.updatePreOrderOf(orderMock, entryMock1);
        verify(inventoryClient, times(1))
                .subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(inventoryClient, times(0))
                .addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()), preOrderCaptor.capture());
        verify(entryMock1).setHoldingStock(10l);
        UpdateInventoryStatusRequest inventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        assertEquals(10, inventoryStatusRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updateStockHoldingProductOfList() {
        List<UpdateProductInventoryDetailData> inventoryDetailList = new ArrayList<>();
        UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
        data.setProductId(12l);
        data.setValue(5l);
        inventoryDetailList.add(data);
        service.updateStockHoldingProductOfList(orderMock, inventoryDetailList, true);
        verify(inventoryClient, times(1)).changeInventoryByStatus(holdingStockCaptor.capture());
        UpdateProductInventoryRequest inventoryStatusRequest = holdingStockCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        assertEquals(5, inventoryStatusRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updatePreOrderProductOfList_hasPreOrder() {
        List<UpdateProductInventoryDetailData> inventoryDetailList = new ArrayList<>();
        UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
        data.setProductId(12l);
        data.setValue(5l);
        inventoryDetailList.add(data);
        service.updatePreOrderProductOfList(orderMock, inventoryDetailList, true);
        verify(inventoryClient, times(1)).addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()),
                preOrderCaptor.capture());
        verify(inventoryClient, times(0)).subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()),
                preOrderCaptor.capture());
        UpdateInventoryStatusRequest inventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        assertEquals(5, inventoryStatusRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void updatePreOrderProductOfList_hasNotPreOrder() {
        List<UpdateProductInventoryDetailData> inventoryDetailList = new ArrayList<>();
        UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
        data.setProductId(12l);
        data.setValue(5l);
        inventoryDetailList.add(data);
        service.updatePreOrderProductOfList(orderMock, inventoryDetailList, false);
        verify(inventoryClient, times(0)).addStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()),
                preOrderCaptor.capture());
        verify(inventoryClient, times(1)).subtractStockWithInventoryStatus(eq(InventoryStatus.PRE_ORDER.code()),
                preOrderCaptor.capture());
        UpdateInventoryStatusRequest inventoryStatusRequest = preOrderCaptor.getValue();
        assertEquals(1, inventoryStatusRequest.getDetailDataList().size());
        assertEquals(5, inventoryStatusRequest.getDetailDataList().get(0).getValue(), 0);
    }

    @Test
    public void validateOutOfStock() {
        try {
            ProductStockData productStock = new ProductStockData();
            productStock.setQuantity(0);
            when(inventoryClient.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(productStock);
            CheckOutOfStockParam param = new CheckOutOfStockParam();
            param.setWarehouseId(1l);
            param.setCompanyId(1l);
            param.setProductId(1l);
            param.setQuantity(1);
            param.setAbstractOrderModel(orderMock);
            when(orderMock.getSellSignal()).thenReturn(SellSignal.WEB.toString());
            service.validateOutOfStock(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_OUT_OF_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validateOutOfStock_Success() {
        ProductStockData productStock = new ProductStockData();
        productStock.setQuantity(1);
        when(inventoryClient.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(productStock);
        CheckOutOfStockParam param = new CheckOutOfStockParam();
        param.setWarehouseId(1l);
        param.setCompanyId(1l);
        param.setProductId(1l);
        param.setQuantity(1);
        param.setAbstractOrderModel(orderMock);
        when(orderMock.getSellSignal()).thenReturn(SellSignal.WEB.toString());
        service.validateOutOfStock(param);
        assertTrue("success", true);
    }
}
