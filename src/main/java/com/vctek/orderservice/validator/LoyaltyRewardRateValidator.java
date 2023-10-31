package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component("loyaltyRewardRateValidator")
public class LoyaltyRewardRateValidator implements Validator<LoyaltyRewardRateRequest> {
    @Override
    public void validate(LoyaltyRewardRateRequest rewardRateRequest) throws ServiceException {
        if(rewardRateRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(CollectionUtils.isEmpty(rewardRateRequest.getDetails())) {
            ErrorCodes err = ErrorCodes.EMPTY_LOYALTY_REWARD_RATE_DATA;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        for (LoyaltyRewardRateDetailRequest detailRequest : rewardRateRequest.getDetails()) {
            if (detailRequest.getRewardRate() == null) {
                ErrorCodes err = ErrorCodes.EMPTY_LOYALTY_REWARD_RATE_DATA;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            if (detailRequest.getRewardRate() < 0) {
                ErrorCodes err = ErrorCodes.INVALID_REWARD_RATE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }
}
