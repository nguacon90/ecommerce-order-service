package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.request.storefront.StoreFrontSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.CommerceCartService;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.util.ComboType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ChangeProductInComboValidatorTest {
    private ChangeProductInComboValidator validator;

    @Mock
    private CommerceCartService commerceCartService;
    @Mock
    private CartService cartService;
    @Mock
    private ProductService productService;
    @Mock
    private StoreFrontSubOrderEntryRequest request;
    @Mock
    private CartModel cart;
    @Mock
    private CartEntryModel cartEntry;
    @Mock
    private SubOrderEntryModel subEntry;
    @Mock
    private SubOrderEntryModel subEntry2;
    @Mock
    private SubOrderEntryModel subEntry3;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ProductStockData stockOfProduct;
    private Set<SubOrderEntryModel> subEntries;
    @Mock
    private ComboData comboData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new ChangeProductInComboValidator();
        validator.setCartService(cartService);
        validator.setProductService(productService);
        validator.setCommerceCartService(commerceCartService);
        validator.setInventoryService(inventoryService);
        subEntries = new HashSet<>();
        when(request.getCompanyId()).thenReturn(1l);
        when(request.getProductId()).thenReturn(1212l);
        when(request.getEntryId()).thenReturn(112l);
        when(request.getSubEntryId()).thenReturn(11l);
        when(request.getOrderCode()).thenReturn("orderCode");
        when(inventoryService.getStoreFrontStockOfProduct(anyLong(), anyLong())).thenReturn(stockOfProduct);
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cart);
        when(cartService.findEntryBy(anyLong(), eq(cart))).thenReturn(cartEntry);
        when(subEntry.getId()).thenReturn(11l);
        when(subEntry.getProductId()).thenReturn(1111l);

        when(subEntry2.getId()).thenReturn(22l);
        when(subEntry2.getProductId()).thenReturn(2222l);

        when(subEntry3.getId()).thenReturn(33l);
        when(subEntry3.getProductId()).thenReturn(3333l);

        subEntries.add(subEntry);
        subEntries.add(subEntry2);
        subEntries.add(subEntry3);
        when(cartEntry.getSubOrderEntries()).thenReturn(subEntries);
    }

    @Test
    public void validate_invalidCart() {
        try {
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.message(), e.getMessage());
        }
    }

    @Test
    public void validate_invalidCartEntry() {
        try {
            when(cartService.findEntryBy(anyLong(), eq(cart))).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ENTRY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_CartEntryIsFixedCombo_NotAcceptForChanging() {
        try {
            when(cartService.isComboEntry(cartEntry)).thenReturn(true);
            when(cartEntry.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
            when(request.getProductId()).thenReturn(111l);
            when(stockOfProduct.getQuantity()).thenReturn(10);

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_CHANGE_COMBO_ENTRY.message(), e.getMessage());
        }
    }

    @Test
    public void validate_CartEntryIsNotCombo_NotAcceptForChanging() {
        try {
            when(cartService.isComboEntry(cartEntry)).thenReturn(false);
            when(request.getProductId()).thenReturn(111l);
            when(stockOfProduct.getQuantity()).thenReturn(10);

            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ACCEPT_CHANGE_ORDER_ENTRY.message(), e.getMessage());
        }
    }

    @Test
    public void validate_invalidSubEntry() {
        try {
            when(cartService.isComboEntry(cartEntry)).thenReturn(true);
            when(request.getSubEntryId()).thenReturn(111111111l);
            when(cartEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SUB_ORDER_ENTRY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_emptyProductIdForChanging() {
        try {
            when(cartService.isComboEntry(cartEntry)).thenReturn(true);
            when(cartEntry.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
            when(request.getProductId()).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PRODUCT_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_NewProductIsOutOfStock() {
        try {
            when(cartService.isComboEntry(cartEntry)).thenReturn(true);
            when(cartEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
            when(request.getProductId()).thenReturn(1234l);
            when(stockOfProduct.getQuantity()).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.PRODUCT_OUT_OF_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate_MultiGroup_NewProductIdExisted() {
        try {

            when(cartService.isComboEntry(cartEntry)).thenReturn(true);
            when(cartEntry.getComboType()).thenReturn(ComboType.MULTI_GROUP.toString());
            when(subEntry.getProductId()).thenReturn(1111l);
            when(subEntry3.getProductId()).thenReturn(3333l);
            when(subEntry2.getProductId()).thenReturn(1234l);

            when(request.getProductId()).thenReturn(1234l);
            when(stockOfProduct.getQuantity()).thenReturn(3);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_PRODUCT_IN_COMBO.message(), e.getMessage());
        }
    }

    @Test
    public void validate_OneGroupGroup_NewProductIdExisted_NotAllowDuplicated() {
        try {

            when(cartService.isComboEntry(cartEntry)).thenReturn(true);
            when(cartEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
            when(subEntry.getProductId()).thenReturn(1111l);
            when(subEntry3.getProductId()).thenReturn(3333l);
            when(subEntry2.getProductId()).thenReturn(1234l);
            when(productService.getCombo(anyLong(), anyLong())).thenReturn(comboData);
            when(comboData.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
            when(comboData.isDuplicateSaleProduct()).thenReturn(false);

            when(request.getProductId()).thenReturn(1234l);
            when(stockOfProduct.getQuantity()).thenReturn(3);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_PRODUCT_IN_COMBO.message(), e.getMessage());
        }
    }

    @Test
    public void validate_success() {
        when(cartService.isComboEntry(cartEntry)).thenReturn(true);
        when(cartEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(request.getProductId()).thenReturn(1234l);
        when(stockOfProduct.getQuantity()).thenReturn(1);
        validator.validate(request);
        assertTrue("success", true);
    }
}
