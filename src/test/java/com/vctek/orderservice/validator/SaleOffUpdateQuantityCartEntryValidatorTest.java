package com.vctek.orderservice.validator;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.InventoryService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class SaleOffUpdateQuantityCartEntryValidatorTest {
    private SaleOffUpdateQuantityCartEntryValidator validator;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private EntryRepository entryRepository;
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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new SaleOffUpdateQuantityCartEntryValidator();
        validator.setCartService(cartService);
        validator.setEntryRepository(entryRepository);
        validator.setInventoryService(inventoryService);
        when(cartEntryModel.getId()).thenReturn(entryId);
        when(cartEntryModel.getProductId()).thenReturn(113l);
        when(cartEntryModel.isSaleOff()).thenReturn(true);
        when(param.getEntryId()).thenReturn(entryId);
        when(param.getOrder()).thenReturn(cartModel);
        when(cartModel.getCompanyId()).thenReturn(1l);
        when(cartModel.getWarehouseId()).thenReturn(406l);
        when(cartModel.getEntries()).thenReturn(Arrays.asList(cartEntryModel));

    }

    @Test
    public void validate_success_NotSaleOffEntry() {
        when(cartEntryModel.isSaleOff()).thenReturn(false);
        when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);

        when(brokenStock.getQuantity()).thenReturn(5);
        when(cartEntryModel.getQuantity()).thenReturn(3l);

        validator.validate(param);
        assertTrue("success", true);
    }

    @Test
    public void validate_success() {
        when(entryRepository.findByIdAndOrder(entryId, cartModel)).thenReturn(cartEntryModel);
        when(inventoryService.getBrokenStock(anyLong(), anyLong(), anyLong())).thenReturn(brokenStock);
        when(param.getQuantity()).thenReturn(4L);
        when(brokenStock.getQuantity()).thenReturn(5);
        when(cartEntryModel.getQuantity()).thenReturn(1l);

        validator.validate(param);
        assertTrue("success", true);
    }

}
