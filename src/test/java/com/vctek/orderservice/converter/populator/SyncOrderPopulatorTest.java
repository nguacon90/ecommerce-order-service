package com.vctek.orderservice.converter.populator;

import com.vctek.migration.dto.MigrateBillDetailDto;
import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.migration.dto.SyncSubOrderEntryData;
import com.vctek.orderservice.converter.populator.migration.SyncOrderPopulator;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.OrderSourceService;
import com.vctek.redis.PriceData;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class SyncOrderPopulatorTest {
    private SyncOrderPopulator populator;
    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private MigrateBillDto migrateBillDtoMock;
    @Mock
    private MigrateBillDetailDto migrateBillDetailDtoMock;
    @Mock
    private SyncSubOrderEntryData subOrderEntryMock;
    @Mock
    private OrderSourceService orderSourceService;
    private ProductSearchModel productMock1;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        productMock1 = new ProductSearchModel();
        populator = new SyncOrderPopulator();
        populator.setProductService(productSearchService);
        populator.setOrderSourceService(orderSourceService);
        when(migrateBillDetailDtoMock.getProductSku()).thenReturn("sku");
        when(migrateBillDetailDtoMock.getType()).thenReturn("type");
        when(migrateBillDetailDtoMock.getPrice()).thenReturn(20000d);
        when(migrateBillDetailDtoMock.getQuantity()).thenReturn(1);
        when(migrateBillDetailDtoMock.getEntryNumber()).thenReturn(0);
        when(migrateBillDetailDtoMock.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        when(migrateBillDtoMock.getOrderSourceId()).thenReturn(1l);
        productMock1.setId(123l);
        productMock1.setSku("sku");
        PriceData priceData = new PriceData();
        priceData.setPrice(20000d);
        productMock1.setPrices(Arrays.asList(priceData));
        when(subOrderEntryMock.getId()).thenReturn(1l);
        when(subOrderEntryMock.getProductSku()).thenReturn("sku");
        when(subOrderEntryMock.getPrice()).thenReturn(20000d);
        when(subOrderEntryMock.getQuantity()).thenReturn(1);
        when(subOrderEntryMock.getFinalPrice()).thenReturn(20000d);
    }

    @Test
    public void populate() {
        OrderModel orderModel = new OrderModel();
        when(migrateBillDtoMock.getCompanyId()).thenReturn(1l);
        when(migrateBillDtoMock.getOrderCode()).thenReturn("code");
        when(migrateBillDtoMock.getTotalCost()).thenReturn(20000d);
        when(migrateBillDtoMock.getWarehouseId()).thenReturn(1l);
        when(migrateBillDtoMock.getType()).thenReturn("order");
        when(migrateBillDtoMock.getCustomerId()).thenReturn(1l);
        when(migrateBillDtoMock.getOrderStatus()).thenReturn(OrderStatus.COMPLETED.code());
        when(migrateBillDtoMock.getFinalCost()).thenReturn(20000d);
        when(migrateBillDtoMock.getDetailDtos()).thenReturn(Arrays.asList(migrateBillDetailDtoMock));
        when(migrateBillDetailDtoMock.getSubOrderEntries()).thenReturn(Arrays.asList(subOrderEntryMock));
        when(productSearchService.findByExternalIdAndCompanyId(anyLong(), anyLong())).thenReturn(productMock1);
        populator.populate(migrateBillDtoMock, orderModel);
        assertEquals(1l, orderModel.getCompanyId(), 0);
        assertEquals("code", orderModel.getCode());
        assertEquals(20000d, orderModel.getTotalPrice(), 0);
        assertEquals(1l, orderModel.getWarehouseId(), 0);
        assertEquals(1l, orderModel.getCustomerId(), 0);
        assertEquals(OrderStatus.COMPLETED.code(), orderModel.getOrderStatus());
        for (AbstractOrderEntryModel entryModel : orderModel.getEntries()) {
            assertEquals(123l, entryModel.getProductId(), 0);
            assertEquals(1, entryModel.getQuantity(), 0);
            assertEquals(20000d, entryModel.getBasePrice(), 0);
            assertEquals(1, entryModel.getQuantity(), 0);
            assertEquals(0, entryModel.getEntryNumber(), 0);
            for (SubOrderEntryModel subOrderEntryModel : entryModel.getSubOrderEntries()) {
                assertEquals(123l, subOrderEntryModel.getProductId(), 0);
                assertEquals(1, subOrderEntryModel.getQuantity(), 0);
            }
        }
    }
}
