package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.repository.OrderSettingRepository;
import com.vctek.orderservice.service.ComboPriceSettingService;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.OrderSettingType;
import org.springframework.stereotype.Service;

@Service
public class ComboPriceSettingServiceImpl implements ComboPriceSettingService {
    private OrderSettingRepository orderSettingRepository;

    public ComboPriceSettingServiceImpl(OrderSettingRepository orderSettingRepository) {
        this.orderSettingRepository = orderSettingRepository;
    }

    @Override
    public OrderSettingModel save(OrderSettingRequest orderSettingRequest) {
        OrderSettingModel existedModel = orderSettingRepository.findByTypeAndCompanyId(OrderSettingType.COMBO_PRICE_SETTING.code(),
                orderSettingRequest.getCompanyId());
        OrderSettingModel model = existedModel != null ? existedModel : new OrderSettingModel();
        populateModel(model, orderSettingRequest);
        return orderSettingRepository.save(model);
    }

    @Override
    public OrderSettingModel findByTypeAndCompanyId(String type, Long companyId) {
        return orderSettingRepository.findByTypeAndCompanyId(type, companyId);
    }

    private void populateModel(OrderSettingModel model, OrderSettingRequest orderSettingRequest) {
        model.setAmount(orderSettingRequest.getAmount());
        model.setCompanyId(orderSettingRequest.getCompanyId());
        model.setAmountType(CurrencyType.PERCENT.toString());
        model.setType(OrderSettingType.COMBO_PRICE_SETTING.code());
    }
}
