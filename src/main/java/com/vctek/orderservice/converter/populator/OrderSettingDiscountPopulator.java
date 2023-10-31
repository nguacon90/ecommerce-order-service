package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import org.springframework.stereotype.Component;

@Component
public class OrderSettingDiscountPopulator implements Populator<OrderSettingDiscountModel, OrderSettingDiscountData> {

    @Override
    public void populate(OrderSettingDiscountModel source, OrderSettingDiscountData target) {
        target.setId(source.getId());
        target.setCompanyId(source.getCompanyId());
        target.setProductId(source.getProductId());
        target.setCategoryCode(source.getCategoryCode());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
        target.setDeleted(source.isDeleted());
    }
}
