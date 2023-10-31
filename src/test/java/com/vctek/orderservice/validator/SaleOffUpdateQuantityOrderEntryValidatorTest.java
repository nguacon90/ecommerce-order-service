package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class SaleOffUpdateQuantityOrderEntryValidatorTest {
    private SaleOffUpdateQuantityOrderEntryValidator validator;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private CommerceAbstractOrderParameter param;

    @Mock
    private OrderModel orderModel;
    @Mock
    private OrderEntryModel orderEntryModel;
    private Long entryId = 11l;
    @Mock
    private ProductStockData brokenStock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new SaleOffUpdateQuantityOrderEntryValidator();
        validator.setOrderService(orderService);
        validator.setEntryRepository(entryRepository);
        validator.setInventoryService(inventoryService);
        when(orderEntryModel.getId()).thenReturn(entryId);
        when(orderEntryModel.getProductId()).thenReturn(113l);
        when(orderEntryModel.isSaleOff()).thenReturn(true);
        when(param.getEntryId()).thenReturn(entryId);
        when(param.getOrder()).thenReturn(orderModel);
        when(orderModel.getCompanyId()).thenReturn(1l);
        when(orderModel.getWarehouseId()).thenReturn(406l);
        when(orderModel.getEntries()).thenReturn(Arrays.asList(orderEntryModel));

    }

    @Test
    public void validate_HasNotBrokenStock() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, orderModel)).thenReturn(orderEntryModel);
            when(orderService.isComboEntry(orderEntryModel)).thenReturn(false);
            when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
            when(brokenStock.getQuantity()).thenReturn(-1);

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_OUT_OF_BROKEN_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_EntryQtyOverBrokenStock() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, orderModel)).thenReturn(orderEntryModel);
            when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
            when(orderEntryModel.getQuantity()).thenReturn(3l);
            when(brokenStock.getQuantity()).thenReturn(2);
            when(param.getQuantity()).thenReturn(6L);

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ENTRY_QUANTITY_OVER_BROKEN_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_success_NotSaleOffEntry() {
        when(orderEntryModel.isSaleOff()).thenReturn(false);
        when(entryRepository.findByIdAndOrder(entryId, orderModel)).thenReturn(orderEntryModel);

        when(brokenStock.getQuantity()).thenReturn(5);
        when(orderEntryModel.getQuantity()).thenReturn(3l);

        validator.validate(param);
        assertTrue("success", true);
    }

    @Test
    public void validate_success() {
        when(entryRepository.findByIdAndOrder(entryId, orderModel)).thenReturn(orderEntryModel);
        when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
        when(param.getQuantity()).thenReturn(4L);
        when(brokenStock.getQuantity()).thenReturn(5);
        when(orderEntryModel.getQuantity()).thenReturn(1l);

        validator.validate(param);
        assertTrue("success", true);
    }

}
