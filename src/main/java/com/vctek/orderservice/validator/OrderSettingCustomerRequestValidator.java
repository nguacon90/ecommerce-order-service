package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.service.OrderSettingCustomerService;
import com.vctek.orderservice.util.CommonUtils;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderSettingCustomerRequestValidator implements Validator<OrderSettingCustomerRequest> {
    private OrderSettingCustomerService service;
    private static List<String> orderTypes = Arrays.asList(
            OrderType.RETAIL.toString(), OrderType.WHOLESALE.toString(), OrderType.ONLINE.toString()
    );

    @Override
    public void validate(OrderSettingCustomerRequest request) throws ServiceException {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getPriority() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_SETTING_CUSTOMER_PRIORITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getPriority() < 0) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER_PRIORITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateName(request.getName(), ErrorCodes.EMPTY_ORDER_SETTING_CUSTOMER_NAME);
        validateOrderType(request);
        List<OrderSettingCustomerModel> models = service.findAllByNameAndCompanyId(request.getName().trim(), request.getCompanyId());
        if (request.getId() != null) {
            OrderSettingCustomerModel model = service.findByIdAndCompanyId(request.getId(), request.getCompanyId());
            if (model == null) {
                ErrorCodes err = ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            models = models.stream().filter(i -> !i.getId().equals(request.getId())).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(models)) {
            ErrorCodes err = ErrorCodes.UNIQUE_ORDER_SETTING_CUSTOMER_NAME;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateOptions(request);
    }

    private void validateOrderType(OrderSettingCustomerRequest request) {
        if (CollectionUtils.isEmpty(request.getOrderTypes())) return;
        for (String orderType : request.getOrderTypes()) {
            if (!orderTypes.contains(orderType)) {
                ErrorCodes err = ErrorCodes.INVALID_ORDER_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    private void validateName(String name, ErrorCodes errorCodes) {
        if (StringUtils.isBlank(name)) {
            throw new ServiceException(errorCodes.code(), errorCodes.message(), errorCodes.httpStatus());
        }

        if(name.trim().length() > CommonUtils.MAXIMUM_LENGTH_100) {
            ErrorCodes err = ErrorCodes.OVER_MAX_LENGTH_100;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validateOptions(OrderSettingCustomerRequest request) {
        if (CollectionUtils.isEmpty(request.getOptions())) {
            ErrorCodes err = ErrorCodes.CANNOT_EMPTY_ORDER_SETTING_CUSTOMER_OPTIONS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        for (OrderSettingCustomerRequest optionRequest : request.getOptions()) {
            validateName(optionRequest.getName(), ErrorCodes.EMPTY_ORDER_SETTING_CUSTOMER_OPTION_NAME);
            validateOptionName(optionRequest, request.getOptions());
        }
    }

    private void validateOptionName(OrderSettingCustomerRequest optionRequest, List<OrderSettingCustomerRequest> options) {
        for (OrderSettingCustomerRequest option : options) {
            if (!optionRequest.equals(option) && optionRequest.getName().trim().equals(option.getName().trim())) {
                ErrorCodes err = ErrorCodes.UNIQUE_ORDER_SETTING_CUSTOMER_OPTION_NAME;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    @Autowired
    public void setService(OrderSettingCustomerService service) {
        this.service = service;
    }
}
