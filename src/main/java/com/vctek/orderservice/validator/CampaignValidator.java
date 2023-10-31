package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CampaignData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.service.CampaignService;
import com.vctek.orderservice.util.ActiveStatus;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CampaignValidator implements Validator<CampaignData> {

    private CampaignService campaignService;

    public CampaignValidator(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Override
    public void validate(CampaignData campaignData) throws ServiceException {
        if(campaignData.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(StringUtils.isBlank(campaignData.getName())) {
            ErrorCodes err = ErrorCodes.EMPTY_CAMPAIGN_NAME;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        ActiveStatus status = ActiveStatus.findByCode(campaignData.getStatus());
        if(status == null) {
            ErrorCodes err = ErrorCodes.INVALID_CAMPAIGN_STATUS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(campaignData.getId() != null) {
            CampaignModel campaignModel = campaignService.findById(campaignData.getId());
            if(campaignModel == null) {
                ErrorCodes err = ErrorCodes.INVALID_CAMPAIGN_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }
}
