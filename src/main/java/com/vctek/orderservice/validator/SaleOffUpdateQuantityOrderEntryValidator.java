package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("saleOffUpdateQuantityOrderEntryValidator")
public class SaleOffUpdateQuantityOrderEntryValidator extends SaleOffEntryValidator {
    private OrderService orderService;

    @Override
    public void validate(CommerceAbstractOrderParameter parameter) {
        AbstractOrderModel order = parameter.getOrder();
        AbstractOrderEntryModel entryModel = getValidatedEntry(parameter, order);
        long diffQty = parameter.getQuantity() - entryModel.getQuantity();
        if (!entryModel.isSaleOff() || diffQty < 0) return;
        validateStockSaleOff(order, entryModel, diffQty);
    }

    private void validateStockSaleOff(AbstractOrderModel order, AbstractOrderEntryModel entryModel, long quantity) {
        ProductStockData brokenStock = inventoryService.getBrokenStock(entryModel.getProductId(), order.getCompanyId(), order.getWarehouseId());
        int brokenQty = CommonUtils.readValue(brokenStock.getQuantity());
        if(brokenQty < 0) {
            ErrorCodes err = ErrorCodes.PRODUCT_OUT_OF_BROKEN_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(quantity > brokenQty) {
            ErrorCodes err = ErrorCodes.ENTRY_QUANTITY_OVER_BROKEN_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{brokenQty + entryModel.getQuantity()});
        }
    }

    @Override
    protected boolean isComboEntry(AbstractOrderEntryModel entryModel) {
        return orderService.isComboEntry(entryModel);
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
