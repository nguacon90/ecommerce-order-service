package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.PromotionStatusRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromotionStatusRequestValidator implements Validator<PromotionStatusRequest> {
    private PromotionSourceRuleService promotionSourceRuleService;

    @Override
    public void validate(PromotionStatusRequest promotionStatusRequest) throws ServiceException {
        if(promotionStatusRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        PromotionSourceRuleModel sourceRule = promotionSourceRuleService
                .findByIdAndCompanyId(promotionStatusRequest.getPromotionId(), promotionStatusRequest.getCompanyId());
        if(sourceRule == null) {
            ErrorCodes err = ErrorCodes.INVALID_PROMOTION_SOURCE_RULE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setPromotionSourceRuleService(PromotionSourceRuleService promotionSourceRuleService) {
        this.promotionSourceRuleService = promotionSourceRuleService;
    }
}
