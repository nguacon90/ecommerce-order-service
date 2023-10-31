package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CampaignData;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import org.springframework.stereotype.Component;

@Component
public class CampaignPopulator implements Populator<CampaignModel, CampaignData> {

    @Override
    public void populate(CampaignModel campaignModel, CampaignData campaignData) {
        campaignData.setId(campaignModel.getId());
        campaignData.setCompanyId(campaignModel.getCompanyId());
        campaignData.setName(campaignModel.getName());
        campaignData.setStatus(campaignModel.getStatus());
    }
}
