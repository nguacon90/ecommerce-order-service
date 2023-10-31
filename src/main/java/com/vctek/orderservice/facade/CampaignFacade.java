package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.CampaignData;

import java.util.List;

public interface CampaignFacade {
    CampaignData createNew(CampaignData campaignData);

    List<CampaignData> findAll(Long companyId, String status);

    CampaignData update(CampaignData campaignData);
}
