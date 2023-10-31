package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("commerceSubOrderEntryParameterPopulator")
public class CommerceSubOrderEntryParameterPopulator implements Populator<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> {
    private OrderService orderService;

    @Override
    public void populate(AddSubOrderEntryRequest source, CommerceAbstractOrderParameter target) {
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(source.getOrderCode(), source.getCompanyId(), false);
        if (orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        target.setCompanyId(orderModel.getCompanyId());
        target.setOrder(orderModel);
        target.setWarehouseId(orderModel.getWarehouseId());
        target.setProductId(source.getProductId());
        target.setEntryId(source.getEntryId());
        target.setComboId(source.getComboId());
        target.setComboGroupNumber(source.getComboGroupNumber());
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
