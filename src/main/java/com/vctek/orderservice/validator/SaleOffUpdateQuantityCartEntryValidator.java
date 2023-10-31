package com.vctek.orderservice.validator;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("saleOffUpdateQuantityCartEntryValidator")
public class SaleOffUpdateQuantityCartEntryValidator extends SaleOffEntryValidator {
    private CartService cartService;

    @Override
    public void validate(CommerceAbstractOrderParameter parameter) {
        AbstractOrderModel order = parameter.getOrder();
        AbstractOrderEntryModel entryModel = getValidatedEntry(parameter, order);
        if (!entryModel.isSaleOff() || parameter.getQuantity() == 0) return;
        super.validateStockSaleOff(parameter, entryModel, true, parameter.getQuantity());
    }

    @Override
    protected boolean isComboEntry(AbstractOrderEntryModel entryModel) {
        return cartService.isComboEntry(entryModel);
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
