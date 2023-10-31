package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import com.vctek.orderservice.service.OrderSettingCustomerOptionService;
import com.vctek.orderservice.service.OrderTypeSettingCustomerService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OrderSettingCustomerModelPopulator implements Populator<OrderSettingCustomerRequest, OrderSettingCustomerModel> {

    private OrderSettingCustomerOptionService optionService;
    private OrderTypeSettingCustomerService orderTypeSettingCustomerService;

    @Override
    public void populate(OrderSettingCustomerRequest request, OrderSettingCustomerModel model) {
        List<OrderTypeSettingCustomerModel> oldOrderTypeModel = model.getOrderTypeSettingCustomerModels();
        model.setOrderTypeSettingCustomerModels(new ArrayList<>());
        if (CollectionUtils.isNotEmpty(request.getOrderTypes())) {
            List<OrderTypeSettingCustomerModel> newOrderTypeModel = new ArrayList<>();
            for (String orderType : request.getOrderTypes()) {
                OrderTypeSettingCustomerModel orderTypeSettingCustomerModel = findByOrderType(orderType, oldOrderTypeModel);
                orderTypeSettingCustomerModel.setOrderType(orderType);
                orderTypeSettingCustomerModel.setOrderTypeSettingCustomers(model);
                newOrderTypeModel.add(orderTypeSettingCustomerModel);
            }
            model.setOrderTypeSettingCustomerModels(newOrderTypeModel);
        }

        if (CollectionUtils.isNotEmpty(oldOrderTypeModel)) {
            orderTypeSettingCustomerService.deleteAll(oldOrderTypeModel);
        }
        if (model.isDefault()) return;

        model.setCompanyId(request.getCompanyId());
        model.setName(request.getName().trim());
        model.setPriority(request.getPriority());
        List<OrderSettingCustomerOptionModel> oldOptions = new ArrayList<>();
        if (model.getId() != null) {
            oldOptions = optionService.findByOrderSettingCustomerModel(model);
            oldOptions.forEach(i -> i.setDeleted(true));
        }
        if (CollectionUtils.isNotEmpty(request.getOptions())) {
            for (OrderSettingCustomerRequest optionRequest : request.getOptions()) {
                OrderSettingCustomerOptionModel optionModel = findBySettingOption(optionRequest, oldOptions);
                optionModel.setName(optionRequest.getName().trim());
                optionModel.setDeleted(false);
                if (optionModel.getId() == null) {
                    optionModel.setOrderSettingCustomerModel(model);
                    oldOptions.add(optionModel);
                }
            }
            model.setOptionModels(oldOptions);
        }
    }

    private OrderSettingCustomerOptionModel findBySettingOption(OrderSettingCustomerRequest optionRequest, List<OrderSettingCustomerOptionModel> oldOptions) {
        if (CollectionUtils.isEmpty(oldOptions)) {
            return new OrderSettingCustomerOptionModel();
        }
        Optional<OrderSettingCustomerOptionModel> optionModel = oldOptions.stream()
                .filter(i -> i.getId() != null && i.getId().equals(optionRequest.getId())).findFirst();
        if (optionModel.isPresent()) {
            return optionModel.get();
        }
        return new OrderSettingCustomerOptionModel();
    }

    private OrderTypeSettingCustomerModel findByOrderType(String orderType, List<OrderTypeSettingCustomerModel> orderTypeSettingCustomerModels) {
        if (CollectionUtils.isEmpty(orderTypeSettingCustomerModels)) return new OrderTypeSettingCustomerModel();
        Optional<OrderTypeSettingCustomerModel> optional = orderTypeSettingCustomerModels.stream().filter(i -> i.getOrderType().equals(orderType)).findFirst();
        if (optional.isPresent()) {
            orderTypeSettingCustomerModels.remove(optional.get());
            return optional.get();
        }
        return new OrderTypeSettingCustomerModel();
    }

    @Autowired
    public void setOptionService(OrderSettingCustomerOptionService optionService) {
        this.optionService = optionService;
    }

    @Autowired
    public void setOrderTypeSettingCustomerService(OrderTypeSettingCustomerService orderTypeSettingCustomerService) {
        this.orderTypeSettingCustomerService = orderTypeSettingCustomerService;
    }
}
