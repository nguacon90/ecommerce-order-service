package com.vctek.orderservice.converter.storefront.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.model.CartModel;
import com.vctek.util.CommonUtils;
import org.springframework.stereotype.Component;

@Component("storefrontMiniCartPopulator")
public class StorefrontMiniCartPopulator implements Populator<CartModel, MiniCartData> {
    @Override
    public void populate(CartModel source, MiniCartData target) {
        target.setCompanyId(source.getCompanyId());
        target.setGuid(source.getGuid());
        target.setCode(source.getCode());
        target.setWarehouseId(source.getWarehouseId());
        target.setFinalPrice(CommonUtils.readValue(source.getFinalPrice()));
        target.setTotalQty(source.getEntries().size());
    }
}
