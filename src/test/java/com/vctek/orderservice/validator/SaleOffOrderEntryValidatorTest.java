package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ProductService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class SaleOffOrderEntryValidatorTest {
    private SaleOffOrderEntryValidator validator;

    @Mock
    private InventoryService inventoryService;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private ProductService productService;
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
    @Mock
    private CompanyClient companyClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new SaleOffOrderEntryValidator();
        validator.setOrderService(orderService);
        validator.setEntryRepository(entryRepository);
        validator.setInventoryService(inventoryService);
        validator.setProductService(productService);
        validator.setCompanyClient(companyClient);
        when(orderModel.getEntries()).thenReturn(Arrays.asList(orderEntryModel));
        when(orderEntryModel.getId()).thenReturn(entryId);
        when(orderEntryModel.getProductId()).thenReturn(113l);
        when(param.isSaleOff()).thenReturn(true);
        when(param.getEntryId()).thenReturn(entryId);
        when(param.getOrder()).thenReturn(orderModel);
        when(orderModel.getCompanyId()).thenReturn(1l);
        when(orderModel.getWarehouseId()).thenReturn(406l);
        when(entryRepository.findByIdAndOrder(entryId, orderModel)).thenReturn(orderEntryModel);
        when(orderService.isComboEntry(orderEntryModel)).thenReturn(false);
        when(productService.isFnB(anyLong())).thenReturn(false);
        when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
    }

    @Test
    public void validate_EntryQtyOverBrokenStock() {
        try {
            when(brokenStock.getQuantity()).thenReturn(1);
            when(orderEntryModel.getQuantity()).thenReturn(2l);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ENTRY_QUANTITY_OVER_BROKEN_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_SwitchSaleOffQtyOverAvailableStock() {
        try {
            when(param.isSaleOff()).thenReturn(false);
            when(brokenStock.getQuantity()).thenReturn(1);
            when(orderEntryModel.getQuantity()).thenReturn(2l);
            when(companyClient.checkSellLessZero(anyLong())).thenReturn(false);
            when(inventoryService.getAvailableStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.SWITCH_SALE_OFF_QUANTITY_OVER_AVAILABLE_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_markSaleOff() {
        when(brokenStock.getQuantity()).thenReturn(1);
        when(orderEntryModel.getQuantity()).thenReturn(1l);
        validator.validate(param);
        assertTrue("success", true);
    }
}
