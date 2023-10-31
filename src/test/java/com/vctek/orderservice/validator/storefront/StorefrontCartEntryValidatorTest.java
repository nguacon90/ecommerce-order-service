package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.StorefrontOrderEntryDTO;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.service.ProductService;
import com.vctek.util.ComboType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class StorefrontCartEntryValidatorTest {
    private StorefrontCartEntryValidator validator;
    @Mock
    private StorefrontOrderEntryDTO entryDTO;
    @Mock
    private ProductService productService;
    @Mock
    private ProductIsCombo productCombo;
    @Mock
    private AddSubOrderEntryRequest subEntry1;
    @Mock
    private ComboData comboData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new StorefrontCartEntryValidator();
        validator.setProductService(productService);
        when(entryDTO.getProductId()).thenReturn(11l);
        when(entryDTO.getCompanyId()).thenReturn(2l);
        when(entryDTO.getQuantity()).thenReturn(1l);
        when(entryDTO.getOrderCode()).thenReturn("orderCode");
        when(productService.checkIsCombo(anyLong(), anyLong(), anyInt())).thenReturn(productCombo);
        when(productService.isOnsite(anyLong(), anyLong())).thenReturn(true);
    }

    @Test
    public void validateCombo_productIsNotCombo() {
        when(productCombo.isCombo()).thenReturn(false);
        validator.validate(entryDTO);
        assertTrue("success", true);
    }

    @Test
    public void validateCombo_productIsFixedCombo() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        validator.validate(entryDTO);
        assertTrue("success", true);
    }

    @Test
    public void validateCombo_productIsNotFixedCombo_EmptySubEntries() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        try {
            validator.validate(entryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ENTRY_FOR_COMBO.message(), e.getMessage());
        }
    }

    @Test
    public void validateCombo_productIsNotFixedCombo_SubEntries_InvalidQty() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(entryDTO.getSubOrderEntries()).thenReturn(Arrays.asList(subEntry1));
        when(subEntry1.getQuantity()).thenReturn(null);
        try {
            validator.validate(entryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY.message(), e.getMessage());
        }
    }

    @Test
    public void validateCombo_productIsNotFixedCombo_SubEntries_InvalidQty_case2() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(entryDTO.getSubOrderEntries()).thenReturn(Arrays.asList(subEntry1));
        when(subEntry1.getQuantity()).thenReturn(0);
        try {
            validator.validate(entryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY.message(), e.getMessage());
        }
    }

    @Test
    public void validateCombo_productIsNotFixedCombo_SubEntries_EmptyComboGroupNumber() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
        when(entryDTO.getSubOrderEntries()).thenReturn(Arrays.asList(subEntry1));
        when(subEntry1.getQuantity()).thenReturn(1);
        when(subEntry1.getComboGroupNumber()).thenReturn(null);
        try {
            validator.validate(entryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMBO_GROUP_NUMBER.message(), e.getMessage());
        }
    }

    @Test
    public void validateCombo_NotEnoughItemInCombo() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(entryDTO.getSubOrderEntries()).thenReturn(Arrays.asList(subEntry1));
        when(subEntry1.getQuantity()).thenReturn(2);
        when(subEntry1.getComboGroupNumber()).thenReturn(1);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(comboData.getTotalItemQuantity()).thenReturn(3);

        try {
            validator.validate(entryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ENOUGH_ITEM_IN_COMBO.message(), e.getMessage());
        }
    }

    @Test
    public void validateCombo_OverTotalItemInCombo() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(entryDTO.getSubOrderEntries()).thenReturn(Arrays.asList(subEntry1));
        when(subEntry1.getQuantity()).thenReturn(2);
        when(subEntry1.getComboGroupNumber()).thenReturn(1);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(comboData.getTotalItemQuantity()).thenReturn(1);

        try {
            validator.validate(entryDTO);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.OVER_MAX_ITEM_IN_COMBO.message(), e.getMessage());
        }
    }

    @Test
    public void validateCombo_productIsNotFixedCombo_success() {
        when(productCombo.isCombo()).thenReturn(true);
        when(productCombo.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(entryDTO.getSubOrderEntries()).thenReturn(Arrays.asList(subEntry1));
        when(subEntry1.getQuantity()).thenReturn(3);
        when(subEntry1.getComboGroupNumber()).thenReturn(1);
        when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
        when(comboData.getTotalItemQuantity()).thenReturn(3);

        validator.validate(entryDTO);
        assertTrue("success", true);
    }

}
