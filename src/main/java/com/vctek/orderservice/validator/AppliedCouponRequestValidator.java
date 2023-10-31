package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.AppliedCouponRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class AppliedCouponRequestValidator implements Validator<AppliedCouponRequest> {

    @Override
    public void validate(AppliedCouponRequest appliedCouponRequest) throws ServiceException {
        if(appliedCouponRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(StringUtils.isBlank(appliedCouponRequest.getOrderCode())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(StringUtils.isBlank(appliedCouponRequest.getCouponCode())) {
            ErrorCodes err = ErrorCodes.EMPTY_APPLIED_COUPON_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(appliedCouponRequest.getRedemptionQuantity() != null && appliedCouponRequest.getRedemptionQuantity() <= 0) {
            ErrorCodes err = ErrorCodes.INVALID_COUPON_TOTAL_REDEMPTION_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
