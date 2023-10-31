package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.ProductService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class SaleOffCartEntryValidatorTest {
    private SaleOffCartEntryValidator validator;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private ProductService productService;
    @Mock
    private CartService cartService;
    @Mock
    private CommerceAbstractOrderParameter param;

    @Mock
    private CartModel cartModel;
    @Mock
    private CartEntryModel cartEntryModel;
    private Long entryId = 11l;
    @Mock
    private ProductStockData brokenStock;
    @Mock
    private AbstractOrderEntryModel saleOffEntry1;
    @Mock
    private AbstractOrderEntryModel saleOffEntry2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new SaleOffCartEntryValidator();
        validator.setCartService(cartService);
        validator.setEntryRepository(entryRepository);
        validator.setInventoryService(inventoryService);
        validator.setProductService(productService);
        when(cartModel.getEntries()).thenReturn(Arrays.asList(cartEntryModel));
        when(cartEntryModel.getId()).thenReturn(entryId);
        when(cartEntryModel.getProductId()).thenReturn(113l);
        when(param.isSaleOff()).thenReturn(true);
        when(param.getEntryId()).thenReturn(entryId);
        when(param.getOrder()).thenReturn(cartModel);
        when(cartModel.getCompanyId()).thenReturn(1l);
        when(cartModel.getWarehouseId()).thenReturn(406l);

    }

    @Test
    public void validate_markNotSaleOff() {
        when(param.isSaleOff()).thenReturn(false);
        validator.validate(param);
        verify(entryRepository, times(0)).findByIdAndOrder(anyLong(), any(AbstractOrderModel.class));
    }

    @Test
    public void validate_notValidEntry() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(null);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ENTRY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_FnBProductNotAllowed() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
            when(cartService.isComboEntry(cartEntryModel)).thenReturn(false);
            when(productService.isFnB(anyLong())).thenReturn(true);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_SALE_OFF_FNB_PRODUCT.message(), e.getMessage());
        }
    }

    @Test
    public void validate_ComboEntryNotAllowed() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
            when(productService.isFnB(anyLong())).thenReturn(false);
            when(cartService.isComboEntry(cartEntryModel)).thenReturn(true);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CAN_NOT_SALE_OFF_COMBO.message(), e.getMessage());
        }
    }

    @Test
    public void validate_HasNotBrokenStock() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
            when(productService.isFnB(anyLong())).thenReturn(false);
            when(cartService.isComboEntry(cartEntryModel)).thenReturn(false);
            when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
            when(brokenStock.getQuantity()).thenReturn(0);

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_OUT_OF_BROKEN_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_EntryQtyOverBrokenStock() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
            when(productService.isFnB(anyLong())).thenReturn(false);
            when(cartService.isComboEntry(cartEntryModel)).thenReturn(false);
            when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
            when(cartEntryModel.getQuantity()).thenReturn(3l);
            when(entryRepository.findAllByOrderAndSaleOffAndProductId(cartModel, true, cartEntryModel.getProductId())).thenReturn(Collections.emptyList());
            when(brokenStock.getQuantity()).thenReturn(2);

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ENTRY_QUANTITY_OVER_BROKEN_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_EntryQtyOverBrokenStock_WithExistedSaleOffQty() {
        try {
            when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
            when(productService.isFnB(anyLong())).thenReturn(false);
            when(cartService.isComboEntry(cartEntryModel)).thenReturn(false);
            when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);

            when(brokenStock.getQuantity()).thenReturn(5);
            when(cartEntryModel.getQuantity()).thenReturn(3l);
            when(saleOffEntry1.getQuantity()).thenReturn(2l);
            when(saleOffEntry2.getQuantity()).thenReturn(1l);
            when(entryRepository.findAllByOrderAndSaleOffAndProductId(cartModel, true, cartEntryModel.getProductId())).thenReturn(Arrays.asList(saleOffEntry1, saleOffEntry2));

            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ENTRY_QUANTITY_OVER_BROKEN_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_success_lessThanBrokenStock_WithoutSaleOffEntry() {
        when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
        when(productService.isFnB(anyLong())).thenReturn(false);
        when(cartService.isComboEntry(cartEntryModel)).thenReturn(false);
        when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);

        when(brokenStock.getQuantity()).thenReturn(5);
        when(cartEntryModel.getQuantity()).thenReturn(3l);
        when(entryRepository.findAllByOrderAndSaleOffAndProductId(cartModel, true, cartEntryModel.getProductId())).thenReturn(Collections.EMPTY_LIST);

        validator.validate(param);
        assertTrue("success", true);
    }

    @Test
    public void validate_success_lessThanBrokenStock_WithSaleOffEntry() {
        when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
        when(productService.isFnB(anyLong())).thenReturn(false);
        when(cartService.isComboEntry(cartEntryModel)).thenReturn(false);
        when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);

        when(brokenStock.getQuantity()).thenReturn(5);
        when(cartEntryModel.getQuantity()).thenReturn(1l);
        when(saleOffEntry1.getQuantity()).thenReturn(2l);
        when(saleOffEntry2.getQuantity()).thenReturn(1l);
        when(entryRepository.findAllByOrderAndSaleOffAndProductId(cartModel, true, cartEntryModel.getProductId())).thenReturn(Arrays.asList(saleOffEntry1, saleOffEntry2));

        validator.validate(param);
        assertTrue("success", true);
    }

}
