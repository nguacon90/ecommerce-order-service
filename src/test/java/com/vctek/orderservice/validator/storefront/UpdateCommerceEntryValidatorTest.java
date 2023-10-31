package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.ProductService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class UpdateCommerceEntryValidatorTest {
    private UpdateCommerceEntryValidator validator;
    @Mock
    private EntryRepository entryRepository;

    @Mock
    private ProductService productService;
    @Mock
    private CommerceAbstractOrderParameter param;
    @Mock
    private AbstractOrderModel orderMock;
    @Mock
    private AbstractOrderEntryModel entryMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new UpdateCommerceEntryValidator(entryRepository);
        validator.setProductService(productService);
        when(param.getOrder()).thenReturn(orderMock);
        when(orderMock.getCompanyId()).thenReturn(1l);
        when(param.getEntryId()).thenReturn(11l);
        when(entryMock.getProductId()).thenReturn(2222l);
    }

    @Test
    public void invalidEntry() {
        try {
            when(entryRepository.findByIdAndOrder(11l, orderMock)).thenReturn(null);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ENTRY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void offsiteProduct_UpdateQuantityDiffZero() {
        try {
            when(entryRepository.findByIdAndOrder(11l, orderMock)).thenReturn(entryMock);
            when(productService.isOnsite(2222l, 1l)).thenReturn(false);
            when(param.getQuantity()).thenReturn(1l);
            validator.validate(param);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.OFF_SITE_PRODUCT.message(), e.getMessage());
        }
    }

    @Test
    public void offsiteProduct_UpdateQuantityIsZero() {
        when(entryRepository.findByIdAndOrder(11l, orderMock)).thenReturn(entryMock);
        when(productService.isOnsite(2222l, 1l)).thenReturn(false);
        when(param.getQuantity()).thenReturn(0l);
        validator.validate(param);
        assertTrue("success", true);
    }

    @Test
    public void validate_success() {
        when(entryRepository.findByIdAndOrder(11l, orderMock)).thenReturn(entryMock);
        when(productService.isOnsite(2222l, 1l)).thenReturn(true);
        validator.validate(param);
        assertTrue("success", true);
    }
}
