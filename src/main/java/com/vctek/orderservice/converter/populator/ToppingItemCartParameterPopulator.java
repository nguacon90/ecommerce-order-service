package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("toppingItemCartParameterPopulator")
public class ToppingItemCartParameterPopulator extends AbstractToppingItemParameterPopulator {
    private CartService cartService;

    @Override
    protected AbstractOrderEntryModel getEntry(AbstractOrderModel abstractOrderModel, Long entryId) {
        return cartService.findEntryBy(entryId, (CartModel) abstractOrderModel);
    }

    @Override
    protected AbstractOrderModel getOrderModel(String code, Long companyId) {
        return cartService.findByCodeAndCompanyId(code, companyId);
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
