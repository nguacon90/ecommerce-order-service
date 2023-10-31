package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.CampaignData;
import com.vctek.orderservice.facade.CampaignFacade;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.service.CampaignService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CampaignFacadeImpl implements CampaignFacade {
    private CampaignService campaignService;
    private Converter<CampaignModel, CampaignData> campaignConverter;

    public CampaignFacadeImpl(CampaignService campaignService,
                              Converter<CampaignModel, CampaignData> campaignConverter) {
        this.campaignService = campaignService;
        this.campaignConverter = campaignConverter;
    }

    @Override
    public CampaignData createNew(CampaignData campaignData) {
        CampaignModel model = new CampaignModel();
        populateModel(campaignData, model);
        CampaignModel savedModel = campaignService.save(model);
        campaignData.setId(savedModel.getId());
        return campaignData;
    }

    private void populateModel(CampaignData campaignData, CampaignModel model) {
        model.setCompanyId(campaignData.getCompanyId());
        model.setName(campaignData.getName());
        model.setStatus(campaignData.getStatus());
    }

    @Override
    public List<CampaignData> findAll(Long companyId, String status) {
        List<CampaignModel> models;

        if (StringUtils.isBlank(status)) {
            models = campaignService.findAllByCompanyId(companyId);
        } else {
            models = campaignService.findAllByCompanyIdAndStatus(companyId, status);
        }

        if(CollectionUtils.isEmpty(models)) {
            return new ArrayList<>();
        }

        return campaignConverter.convertAll(models);
    }

    @Override
    public CampaignData update(CampaignData campaignData) {
        CampaignModel model = campaignService.findById(campaignData.getId());
        populateModel(campaignData, model);
        campaignService.save(model);
        return campaignData;
    }
}
