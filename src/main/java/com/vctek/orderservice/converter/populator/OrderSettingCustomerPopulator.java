package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSettingCustomerData;
import com.vctek.orderservice.dto.OrderSettingCustomerOptionData;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderSettingCustomerPopulator implements Populator<OrderSettingCustomerModel, OrderSettingCustomerData> {

    @Override
    public void populate(OrderSettingCustomerModel source, OrderSettingCustomerData target) {
        target.setId(source.getId());
        target.setDefault(source.isDefault());
        target.setCompanyId(source.getCompanyId());
        target.setPriority(source.getPriority());
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedTime(source.getCreatedTime());
        target.setModifiedBy(source.getModifiedBy());
        target.setModifiedTime(source.getModifiedTime());
        target.setName(source.getName());
        if (CollectionUtils.isNotEmpty(source.getOrderTypeSettingCustomerModels())) {
            List<String> orderTypes = source.getOrderTypeSettingCustomerModels().stream().map(i -> i.getOrderType()).collect(Collectors.toList());
            target.setOrderTypes(orderTypes);
        }

        if (CollectionUtils.isNotEmpty(source.getOptionModels())) {
            List<OrderSettingCustomerData> options = new ArrayList<>();
            for (OrderSettingCustomerOptionModel optionModel : source.getOptionModels()) {
                if (optionModel.isDeleted()) continue;
                OrderSettingCustomerData data = new OrderSettingCustomerData();
                data.setId(optionModel.getId());
                data.setName(optionModel.getName());
                options.add(data);
            }
            target.setOptions(options);
        }
    }
}
