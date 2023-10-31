package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.RefreshCartRequest;
import com.vctek.orderservice.dto.request.UserHasWarehouseRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RefreshCartRequestValidatorTest {
    private RefreshCartRequestValidator validator;
    @Mock
    private CheckPermissionClient checkPermissionClient;
    private RefreshCartRequest request = new RefreshCartRequest();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new RefreshCartRequestValidator();
        validator.setCheckPermissionClient(checkPermissionClient);
    }

    @Test
    public void validate_EmptyOldCompany() {
        try {
            request.setCompanyId(1l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_OLD_COMPANY.code(), e.getCode());
        }
    }

    @Test
    public void validate() {
        try {
            request.setOldCompanyId(1l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_UserHasNotPermissionWarehouse() {
        try {
            request.setCompanyId(2l);
            request.setOldCompanyId(2l);
            request.setWarehouseId(22l);
            when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class)))
                    .thenReturn(false);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.USER_HAS_NOT_PERMISSION_ON_WAREHOUSE.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        request.setCompanyId(2l);
        request.setOldCompanyId(2l);
        validator.validate(request);
    }
}
