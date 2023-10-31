package com.vctek.orderservice.validator;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("saleOffCartEntryValidator")
public class SaleOffCartEntryValidator extends SaleOffEntryValidator {
    private CartService cartService;

    @Override
    protected boolean isComboEntry(AbstractOrderEntryModel entryModel) {
        return cartService.isComboEntry(entryModel);
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
