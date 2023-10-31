package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.CampaignData;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.service.CampaignService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CampaignFacadeTest {
    private CampaignFacadeImpl facade;

    @Mock
    private CampaignService campaignService;
    @Mock
    private Converter<CampaignModel, CampaignData> campaignConverter;
    @Mock
    private CampaignData data;
    @Mock
    private CampaignModel model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new CampaignFacadeImpl(campaignService, campaignConverter);
        when(model.getId()).thenReturn(1l);
    }

    @Test
    public void createNew() {
        when(campaignService.save(any(CampaignModel.class))).thenReturn(model);
        facade.createNew(data);

        verify(campaignService).save(any(CampaignModel.class));
    }

    @Test
    public void findAll_NoStatus() {
        facade.findAll(1l, "");
        verify(campaignService).findAllByCompanyId(1l);
        verify(campaignService, times(0)).findAllByCompanyIdAndStatus(anyLong(), anyString());
    }

    @Test
    public void findAll_CompanyIdAndStatus() {
        facade.findAll(1l, "ACTIVE");
        verify(campaignService, times(0)).findAllByCompanyId(1l);
        verify(campaignService).findAllByCompanyIdAndStatus(anyLong(), anyString());
    }

    @Test
    public void update() {
        when(data.getId()).thenReturn(1l);
        when(campaignService.findById(1l)).thenReturn(model);

        facade.update(data);
        verify(campaignService).save(any(CampaignModel.class));
    }
}
