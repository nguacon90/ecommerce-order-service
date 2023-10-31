package com.vctek.orderservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CheckOutOfStockParam;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.strategy.AddToCartStrategy;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;

public abstract class AbstractAddToCartStrategy extends AbstractCommerceCartStrategy implements AddToCartStrategy {

    protected void validateAddToCart(final CommerceAbstractOrderParameter parameter)
    {
        final AbstractOrderModel order = parameter.getOrder();
        if(order == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(parameter.getProductId() == null) {
            ErrorCodes err = ErrorCodes.INVALID_PRODUCT_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (parameter.getQuantity() < 1)
        {
            ErrorCodes err = ErrorCodes.INVALID_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        CheckOutOfStockParam param = new CheckOutOfStockParam();
        param.setCompanyId(parameter.getCompanyId());
        param.setProductId(parameter.getProductId());
        param.setQuantity(parameter.getQuantity());
        param.setWarehouseId(parameter.getWarehouseId());
        param.setAbstractOrderModel(order);
        if(SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(order.getSellSignal())) {
            inventoryService.validateOutOfStock(param);
            parameter.setQuantity(param.getQuantity());
            return;
        }

        Boolean sellLessZero = companyClient.checkSellLessZero(order.getCompanyId());
        if(sellLessZero != null && !sellLessZero) {
            if (validateComboAvailableToSell(parameter)) {
                return;
            }

            inventoryService.validateOutOfStock(param);
        }

    }

    private boolean validateComboAvailableToSell(CommerceAbstractOrderParameter parameter) {
        ProductIsCombo productIsCombo = productService.checkIsCombo(parameter.getProductId(), parameter.getCompanyId(), (int) parameter.getQuantity());
        if(productIsCombo.isCombo()) {
            productService.checkAvailableToSale(productIsCombo, parameter.getOrder());
            return true;
        }

        return false;
    }

    protected void validateValidAddEntryToOnlineOrder(CommerceAbstractOrderParameter parameter) {
        AbstractOrderModel order = parameter.getOrder();
        if((order instanceof CartModel) || !OrderType.ONLINE.toString().equals(order.getType())
            || !OrderStatus.PRE_ORDER.code().equals(order.getOrderStatus())) {
            return;
        }

        Long productId = parameter.getProductId();
        if(productService.isFnB(productId)) {
            ErrorCodes err = ErrorCodes.CANNOT_ADD_FNB_TO_PREORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

    }
}
