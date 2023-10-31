package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.UserHasWarehouseRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.util.PriceType;
import com.vctek.util.OrderType;
import com.vctek.util.WarehouseStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class CreateCartValidatorTest {
    private CreateCartValidator validator;
    private CartInfoParameter param;
    @Mock
    private CheckPermissionClient checkPermissionClient;
    @Mock
    private LogisticService logisticService;
    @Mock
    private WarehouseData warehouseMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        param = new CartInfoParameter();
        validator = new CreateCartValidator();
        validator.setCheckPermissionClient(checkPermissionClient);
        validator.setLogisticService(logisticService);
        when(logisticService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(warehouseMock);
    }

    @Test
    public void emptyCompanyId() {
        try {
            validator.validate(param);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void emptyWarehouseId() {
        try {
            param.setCompanyId(1l);
            validator.validate(param);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_WAREHOUSE_ID.code(), e.getCode());
        }
    }

    @Test
    public void userHasNotPermissionOnWarehouse() {
        try {
            param.setCompanyId(1l);
            param.setWarehouseId(22l);
            when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class))).thenReturn(false);
            validator.validate(param);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.USER_HAS_NOT_PERMISSION_ON_WAREHOUSE.code(), e.getCode());
        }
    }

    @Test
    public void invalidOrderType() {
        try {
            when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class))).thenReturn(true);
            when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
            param.setCompanyId(1l);
            param.setWarehouseId(2l);
            validator.validate(param);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_TYPE.code(), e.getCode());
        }
    }
    @Test
    public void Empty_price_type() {
        try {
            when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class))).thenReturn(true);
            when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
            param.setCompanyId(1l);
            param.setWarehouseId(2l);
            param.setOrderType(OrderType.ONLINE.toString());
            validator.validate(param);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PRICE_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void Invalid_price_type() {
        try {
            when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class))).thenReturn(true);
            when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
            param.setCompanyId(1l);
            param.setWarehouseId(2l);
            param.setOrderType(OrderType.ONLINE.toString());
            param.setPriceType("1");
            validator.validate(param);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PRICE_TYPE.code(), e.getCode());
        }
    }

    @Test
    public void warehouseNotActive() {
        try {
            when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class))).thenReturn(true);
            when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.INACTIVE.code());
            param.setCompanyId(1l);
            param.setWarehouseId(2l);
            param.setOrderType(OrderType.ONLINE.toString());
            param.setPriceType("1");
            validator.validate(param);
            fail("must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INACTIVE_WAREHOUSE.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class))).thenReturn(true);
        when(warehouseMock.getStatus()).thenReturn(WarehouseStatus.ACTIVE.code());
        param.setCompanyId(1l);
        param.setWarehouseId(2l);
        param.setOrderType(OrderType.RETAIL.toString());
        param.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        validator.validate(param);
    }
}
