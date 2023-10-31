package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CampaignData;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CampaignConverter extends AbstractPopulatingConverter<CampaignModel, CampaignData> {

    @Autowired
    private Populator<CampaignModel, CampaignData> campaignPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CampaignData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(campaignPopulator);
    }
}
