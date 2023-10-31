package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.repository.CartEntryRepository;
import org.springframework.stereotype.Component;

@Component("miniCartPopulator")
public class MiniCartPopulator implements Populator<CartModel, MiniCartData> {

    private CartEntryRepository cartEntryRepository;

    public MiniCartPopulator(CartEntryRepository cartEntryRepository) {
        this.cartEntryRepository = cartEntryRepository;
    }

    @Override
    public void populate(CartModel cartModel, MiniCartData miniCartData) {
        miniCartData.setCode(cartModel.getCode());
        miniCartData.setCompanyId(cartModel.getCompanyId());
        miniCartData.setWarehouseId(cartModel.getWarehouseId());
        miniCartData.setFinalPrice(cartModel.getFinalPrice() == null ? 0 : cartModel.getFinalPrice());
        Long totalQuantity = cartEntryRepository.sumTotalQuantity(cartModel.getId());
        miniCartData.setTotalQty(totalQuantity == null ? 0 : totalQuantity);
    }
}
