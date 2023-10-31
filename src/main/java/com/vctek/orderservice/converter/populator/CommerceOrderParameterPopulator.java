package com.vctek.orderservice.converter.populator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("commerceOrderParameterPopulator")
public class CommerceOrderParameterPopulator extends CommerceCartParameterPopulator {

    private OrderService orderService;

    public CommerceOrderParameterPopulator(CartService cartService, AuthService authService,
                                           CheckPermissionClient checkPermissionClient) {
        super(cartService, authService, checkPermissionClient);
    }


    @Override
    public void populateAbstractOrderModel(Long userId, OrderEntryDTO orderEntryDTO, CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        String orderCode = orderEntryDTO.getOrderCode();
        Long companyId = orderEntryDTO.getCompanyId();

        OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        if (order == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        commerceAbtractOrderParameter.setOrder(order);
        commerceAbtractOrderParameter.setWarehouseId(order.getWarehouseId());
        commerceAbtractOrderParameter.setCompanyId(companyId);
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
