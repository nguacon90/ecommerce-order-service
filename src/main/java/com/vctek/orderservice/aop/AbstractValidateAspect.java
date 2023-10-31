package com.vctek.orderservice.aop;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.util.WarehouseStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractValidateAspect {
    protected LogisticService logisticService;

    protected void validateActiveWarehouseOf(AbstractOrderModel abstractOrderModel) {
        if (abstractOrderModel == null) {
            return;
        }
        WarehouseData warehouseData = logisticService.findByIdAndCompanyId(abstractOrderModel.getWarehouseId(), abstractOrderModel.getCompanyId());
        if(warehouseData == null || warehouseData.getStatus() == null || warehouseData.getStatus().equals(WarehouseStatus.INACTIVE.code())) {
            ErrorCodes err = abstractOrderModel instanceof OrderModel ? ErrorCodes.ORDER_WITH_INACTIVE_WAREHOUSE : ErrorCodes.CART_WITH_INACTIVE_WAREHOUSE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }
}
