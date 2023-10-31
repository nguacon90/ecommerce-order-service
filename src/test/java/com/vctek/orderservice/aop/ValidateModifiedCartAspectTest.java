package com.vctek.orderservice.aop;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.util.WarehouseStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ValidateModifiedCartAspectTest {
    private ValidateModifiedCartAspect aspect;

    @Mock
    private CartService cartService;

    @Mock
    private LogisticService logisticService;
    @Mock
    private WarehouseData warehouseMock;
    private String cartCode = "1818181818";
    @Mock
    private OrderEntryDTO orderEntryMock;
    @Mock
    private CartModel cartMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        aspect = new ValidateModifiedCartAspect();
        aspect.setCartService(cartService);
        aspect.setLogisticService(logisticService);
        when(logisticService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(warehouseMock);
        when(orderEntryMock.getCompanyId()).thenReturn(1l);
        when(cartService.findByCodeAndCompanyId(cartCode, 1l)).thenReturn(cartMock);
        when(cartMock.getWarehouseId()).thenReturn(17l);
    }

    @Test
    public void validateAddCartEntry_InActiveWarehouse() {
        try {
            when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.INACTIVE.code());
            aspect.validateAddCartEntry(cartCode, orderEntryMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CART_WITH_INACTIVE_WAREHOUSE.message(), e.getMessage());
        }
    }

    @Test
    public void validateAddCartEntry_ActiveWarehouse() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateAddCartEntry(cartCode, orderEntryMock);
        assertTrue("success", true);
    }

    @Test
    public void validateRemoveCartEntry() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateRemoveCartEntry(cartCode, 11l, 1l, 111l);
        assertTrue("success", true);
    }

    @Test
    public void validateUpdateDiscountForCart() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        CartDiscountRequest cartDiscountReq = new CartDiscountRequest();
        cartDiscountReq.setCompanyId(1l);
        aspect.validateUpdateDiscountForCart(cartCode, cartDiscountReq, 111l);
        assertTrue("success", true);
    }

    @Test
    public void validateUpdateQuantityForCartEntry() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateUpdateQuantityForCartEntry(cartCode, 11l, orderEntryMock, 111l);
        assertTrue("success", true);
    }

    @Test
    public void validateUpdateVatForCart() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        VatRequest vatReq = new VatRequest();
        vatReq.setCompanyId(1l);
        aspect.validateUpdateVatForCart(cartCode, vatReq, 111l);
        assertTrue("success", true);
    }

    @Test
    public void validateUpdateWeightForCartEntry() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateUpdateWeightForCartEntry(cartCode, 111l, orderEntryMock);
        assertTrue("success", true);
    }

    @Test
    public void validateApplyCoupon() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        AppliedCouponRequest couponReq = new AppliedCouponRequest();
        couponReq.setCompanyId(1l);
        aspect.validateApplyCoupon(cartCode, couponReq);
        assertTrue("success", true);
    }

    @Test
    public void validateAddProductToCombo() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        AddSubOrderEntryRequest req = new AddSubOrderEntryRequest();
        req.setCompanyId(1l);
        aspect.validateAddProductToCombo(cartCode, 11l, req);
        assertTrue("success", true);
    }

    @Test
    public void validateRemoveSubEntry() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        RemoveSubOrderEntryRequest req = new RemoveSubOrderEntryRequest();
        req.setCompanyId(1l);
        aspect.validateRemoveSubEntry(cartCode, 11l, req);
        assertTrue("success", true);
    }

    @Test
    public void validateAppliedPromotion() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateAppliedPromotion(cartCode, 1l, 123l);
        assertTrue("success", true);
    }

    @Test
    public void validateAddToppingOption() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        ToppingOptionRequest req = new ToppingOptionRequest();
        req.setCompanyId(1l);
        aspect.validateAddToppingOption(cartCode, 1l, req);
        assertTrue("success", true);
    }

    @Test
    public void validateUpdateToppingOption() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        ToppingOptionRequest req = new ToppingOptionRequest();
        req.setCompanyId(1l);
        aspect.validateUpdateToppingOption(cartCode, 11l, 111l, req, 1111l);
        assertTrue("success", true);
    }

    @Test
    public void validateRemoveToppingOption() {
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateRemoveToppingOption(cartCode, 11l, 111l, 1l);
        assertTrue("success", true);
    }

    @Test
    public void validateAddToppingItem() {
        ToppingItemRequest req = new ToppingItemRequest();
        req.setCompanyId(1l);
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateAddToppingItem(cartCode, 11l, req, 111l);
        assertTrue("success", true);
    }

    @Test
    public void validateUpdateToppingItem() {
        ToppingItemRequest req = new ToppingItemRequest();
        req.setCompanyId(1l);
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateUpdateToppingItem(cartCode, 11l, 111l, 11111l, req, 111111l);
        assertTrue("success", true);
    }

    @Test
    public void validateUpdatePriceType() {
        CartInfoParameter req = new CartInfoParameter();
        req.setCompanyId(1l);
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        aspect.validateUpdatePriceType(cartCode, req);
        assertTrue("success", true);
    }
}
