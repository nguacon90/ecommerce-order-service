package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.model.OrderSettingModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSettingPopulator implements Populator<OrderSettingModel, OrderSettingData> {

    private Converter<OrderSettingDiscountModel, OrderSettingDiscountData> orderSettingDiscountConverter;
    @Override
    public void populate(OrderSettingModel orderSettingModel, OrderSettingData orderSettingData) {
        orderSettingData.setId(orderSettingModel.getId());
        orderSettingData.setCompanyId(orderSettingModel.getCompanyId());
        orderSettingData.setAmountType(orderSettingModel.getAmountType());
        orderSettingData.setType(orderSettingModel.getType());
        orderSettingData.setAmount(orderSettingModel.getAmount());
        orderSettingData.setOrderTypes(orderSettingModel.getOrderTypes());
        orderSettingData.setOrderStatus(orderSettingModel.getOrderStatus());
        orderSettingData.setNote(orderSettingModel.getNote());

        if (CollectionUtils.isNotEmpty(orderSettingModel.getSettingDiscountModel())) {
            orderSettingData.setSettingDiscountData(orderSettingDiscountConverter.convertAll(orderSettingModel.getSettingDiscountModel()));
        }
    }

    @Autowired
    public void setOrderSettingDiscountConverter(Converter<OrderSettingDiscountModel, OrderSettingDiscountData> orderSettingDiscountConverter) {
        this.orderSettingDiscountConverter = orderSettingDiscountConverter;
    }
}
