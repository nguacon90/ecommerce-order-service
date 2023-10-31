package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.request.CouponRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.CouponService;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CouponRequestValidator implements Validator<CouponRequest> {
    private CouponService couponService;

    @Override
    public void validate(CouponRequest couponRequest) throws ServiceException {
        if(couponRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(couponRequest.getId() != null) {
            CouponModel couponModel = couponService.findById(couponRequest.getId(), couponRequest.getCompanyId());
            if(couponModel == null) {
                ErrorCodes err = ErrorCodes.INVALID_COUPON_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

        if(StringUtils.isBlank(couponRequest.getName())) {
            ErrorCodes err = ErrorCodes.EMPTY_COUPON_NAME;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Integer length = couponRequest.getLength();
        if(length == null || length <= 0) {
            ErrorCodes err = ErrorCodes.INVALID_COUPON_LENGTH;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Integer quantity = couponRequest.getQuantity();
        if(quantity == null || quantity <= 0) {
            ErrorCodes err = ErrorCodes.INVALID_COUPON_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Integer maxTotalRedemption = couponRequest.getMaxTotalRedemption();
        if(maxTotalRedemption == null || maxTotalRedemption <= 0) {
            ErrorCodes err = ErrorCodes.INVALID_COUPON_MAX_TOTAL_REDEMPTION;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(CollectionUtils.isEmpty(couponRequest.getCodes())) {
            ErrorCodes err = ErrorCodes.EMPTY_COUPON_CODES;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }
}
