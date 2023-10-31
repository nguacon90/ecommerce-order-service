package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.UserHasWarehouseRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.util.WarehouseStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCartValidator {
    protected CheckPermissionClient checkPermissionClient;
    protected LogisticService logisticService;
    protected ProductService productService;

    protected void validateCommonProperties(OrderEntryDTO orderEntryDTO) {
        if(orderEntryDTO.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(StringUtils.isBlank(orderEntryDTO.getOrderCode())) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Long productId = orderEntryDTO.getProductId();
        if(productId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(orderEntryDTO.getQuantity() == null || orderEntryDTO.getQuantity() < 1) {
            ErrorCodes err = ErrorCodes.INVALID_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void checkUserManageWarehouse(Long companyId, Long warehouseId) {
        UserHasWarehouseRequest request = new UserHasWarehouseRequest();
        request.setCompanyId(companyId);
        request.setWarehouseId(warehouseId);
        boolean userHasWarehouse = checkPermissionClient.userHasWarehouse(request);

        if (!userHasWarehouse) {
            ErrorCodes err = ErrorCodes.USER_HAS_NOT_PERMISSION_ON_WAREHOUSE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(),
                    new Object[]{warehouseId});
        }
    }

    protected void validateWarehouseStatus(Long warehouseId, Long companyId) {
        WarehouseData warehouseData = logisticService.findByIdAndCompanyId(warehouseId, companyId);
        if(warehouseData == null || warehouseData.getStatus() == null || warehouseData.getStatus().equals(WarehouseStatus.INACTIVE.code())) {
            ErrorCodes err = ErrorCodes.INACTIVE_WAREHOUSE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateOnsiteProduct(Long productId, Long companyId) {
        boolean onsite = productService.isOnsite(productId, companyId);
        if(!onsite) {
            ErrorCodes err = ErrorCodes.OFF_SITE_PRODUCT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setCheckPermissionClient(CheckPermissionClient checkPermissionClient) {
        this.checkPermissionClient = checkPermissionClient;
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
