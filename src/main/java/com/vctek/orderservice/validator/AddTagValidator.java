package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.AddTagRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.TagService;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddTagValidator implements Validator<AddTagRequest> {
    private OrderService orderService;
    private TagService tagService;

    @Override
    public void validate(AddTagRequest request) {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderModel model = orderService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
        if(model == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(request.getTagId() == null) {
            ErrorCodes err = ErrorCodes.INVALID_TAG;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        TagModel tagModel = tagService.findByIdAndCompanyId(request.getTagId(), request.getCompanyId());
        if(tagModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_TAG;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }
}
