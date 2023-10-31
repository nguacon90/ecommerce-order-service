package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSettingCustomerData;
import com.vctek.orderservice.dto.SettingCustomerData;
import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.dto.request.OrderSettingCustomerSearchRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderSettingCustomerFacade;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import com.vctek.orderservice.service.OrderSettingCustomerService;
import com.vctek.orderservice.service.OrderTypeSettingCustomerService;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderSettingCustomerFacadeImpl implements OrderSettingCustomerFacade {
    private OrderSettingCustomerService service;
    private OrderTypeSettingCustomerService orderTypeSettingCustomerService;
    private Converter<OrderSettingCustomerModel, OrderSettingCustomerData> converter;
    private Populator<OrderSettingCustomerRequest, OrderSettingCustomerModel> modelPopulator;

    @Override
    public OrderSettingCustomerData createSetting(OrderSettingCustomerRequest request) {
        OrderSettingCustomerModel model = new OrderSettingCustomerModel();
        modelPopulator.populate(request, model);
        OrderSettingCustomerModel saveModel = service.save(model);
        return converter.convert(saveModel);
    }

    @Override
    public OrderSettingCustomerData updateSetting(OrderSettingCustomerRequest request) {
        OrderSettingCustomerModel model = service.findByIdAndCompanyId(request.getId(), request.getCompanyId());
        modelPopulator.populate(request, model);
        OrderSettingCustomerModel saveModel = service.save(model);
        return converter.convert(saveModel);
    }

    @Override
    public OrderSettingCustomerData findOneBy(Long settingId, Long companyId) {
        OrderSettingCustomerModel model = service.findByIdAndCompanyId(settingId, companyId);
        validateOrderSettingCustomerModel(model);
        return converter.convert(model);
    }

    @Override
    public List<OrderSettingCustomerData> findAllBy(OrderSettingCustomerSearchRequest request) {
        List<OrderSettingCustomerModel> models = service.findAllBy(request);
        return converter.convertAll(models);
    }

    @Override
    public OrderSettingCustomerData createOrUpdateDefault(OrderSettingCustomerRequest request) {
        OrderSettingCustomerModel model = service.findByCompanyIdAndDefault(request.getCompanyId());
        if (model == null) {
            model = new OrderSettingCustomerModel();
            model.setName("Độ tuổi khách mua hàng");
            model.setDefault(true);
            model.setCompanyId(request.getCompanyId());
            model.setPriority(10);
        }
        populateOrderTypeSetting(request, model);
        OrderSettingCustomerModel saveModel = service.save(model);
        return converter.convert(saveModel);
    }

    private void populateOrderTypeSetting(OrderSettingCustomerRequest request, OrderSettingCustomerModel model) {
        List<OrderTypeSettingCustomerModel> oldOrderTypeModel = model.getOrderTypeSettingCustomerModels();
        List<String> orderTypes = Arrays.asList(
                OrderType.RETAIL.toString(), OrderType.WHOLESALE.toString(), OrderType.ONLINE.toString()
        );
        model.setOrderTypeSettingCustomerModels(new ArrayList<>());
        if (CollectionUtils.isNotEmpty(request.getOrderTypes())) {
            List<OrderTypeSettingCustomerModel> newOrderTypeModel = new ArrayList<>();
            for (String orderType : request.getOrderTypes()) {
                if (!orderTypes.contains(orderType)) {
                    ErrorCodes err = ErrorCodes.INVALID_ORDER_TYPE;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus());
                }
                OrderTypeSettingCustomerModel orderTypeSettingCustomerModel;
                Optional<OrderTypeSettingCustomerModel> optional = oldOrderTypeModel.stream().filter(i -> i.getOrderType().equals(orderType)).findFirst();
                if (optional.isPresent()) {
                    oldOrderTypeModel.remove(optional.get());
                    orderTypeSettingCustomerModel = optional.get();
                } else {
                    orderTypeSettingCustomerModel = new OrderTypeSettingCustomerModel();
                }
                orderTypeSettingCustomerModel.setOrderType(orderType);
                orderTypeSettingCustomerModel.setOrderTypeSettingCustomers(model);
                newOrderTypeModel.add(orderTypeSettingCustomerModel);
            }
            model.setOrderTypeSettingCustomerModels(newOrderTypeModel);
        }

        if (CollectionUtils.isNotEmpty(oldOrderTypeModel)) {
            orderTypeSettingCustomerService.deleteAll(oldOrderTypeModel);
        }
    }

    private void validateOrderSettingCustomerModel(OrderSettingCustomerModel model) {
        if (model == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public OrderSettingCustomerData getSettingDefault(Long companyId) {
        OrderSettingCustomerModel model = service.findByCompanyIdAndDefault(companyId);
        if (model != null) {
            return converter.convert(model);
        }
        return null;
    }

    @Override
    public void deletedSetting(Long id, Long companyId) {
        OrderSettingCustomerModel model = service.findByIdAndCompanyId(id, companyId);
        validateOrderSettingCustomerModel(model);
        if (CollectionUtils.isNotEmpty(model.getOrderTypeSettingCustomerModels())) {
            orderTypeSettingCustomerService.deleteAll(model.getOrderTypeSettingCustomerModels());
        }
        model.getOptionModels().forEach(i -> i.setDeleted(true));
        model.setDeleted(true);
        service.save(model);
    }

    @Override
    public void deletedSettingOption(Long settingId, Long optionId, Long companyId) {
        OrderSettingCustomerModel model = service.findByIdAndCompanyId(settingId, companyId);
        validateOrderSettingCustomerModel(model);
        Optional<OrderSettingCustomerOptionModel> optional = model.getOptionModels().stream().filter(i -> i.getId().equals(optionId)).findFirst();
        if (!optional.isPresent()) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER_OPTIONS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        OrderSettingCustomerOptionModel optionModel = optional.get();
        optionModel.setDeleted(true);
        service.save(model);
    }

    @Override
    public SettingCustomerData findSettingByOrder(Long companyId, String orderType) {
        List<OrderSettingCustomerModel> modelList = service.findAllByCompanyIdAndOrderType(companyId, orderType);
        OrderSettingCustomerModel defaultModel = service.findByCompanyIdAndDefault(companyId);
        SettingCustomerData data = new SettingCustomerData();
        if (CollectionUtils.isEmpty(modelList) && defaultModel == null) {
            data.setDefaultSetting(true);
            return data;
        }

        if (defaultModel == null) {
            data.setDefaultSetting(true);
        } else {
            Optional<OrderTypeSettingCustomerModel> optionalDefault = defaultModel.getOrderTypeSettingCustomerModels().stream().filter(i -> i.getOrderType().equals(orderType)).findFirst();
            if (optionalDefault.isPresent()) {
                data.setDefaultSetting(true);
            }
        }

        if (CollectionUtils.isNotEmpty(modelList)) {
            List<OrderSettingCustomerData> dataList = converter.convertAll(modelList);
            data.setSettings(dataList);
        }
        return data;
    }

    @Autowired
    public void setService(OrderSettingCustomerService service) {
        this.service = service;
    }

    @Autowired
    public void setConverter(Converter<OrderSettingCustomerModel, OrderSettingCustomerData> converter) {
        this.converter = converter;
    }

    @Autowired
    public void setModelPopulator(Populator<OrderSettingCustomerRequest, OrderSettingCustomerModel> modelPopulator) {
        this.modelPopulator = modelPopulator;
    }

    @Autowired
    public void setOrderTypeSettingCustomerService(OrderTypeSettingCustomerService orderTypeSettingCustomerService) {
        this.orderTypeSettingCustomerService = orderTypeSettingCustomerService;
    }
}

