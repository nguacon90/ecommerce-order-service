package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CampaignData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.CampaignService;
import com.vctek.orderservice.util.ActiveStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class CampaignValidatorTest {
    private CampaignValidator validator;

    @Mock
    private CampaignService campaignService;

    @Mock
    private CampaignData data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new CampaignValidator(campaignService);
    }

    @Test
    public void validate_emptyCompanyId() {
        try {
            when(data.getCompanyId()).thenReturn(null);
            validator.validate(data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyName() {
        try {
            when(data.getCompanyId()).thenReturn(1l);
            when(data.getName()).thenReturn(null);

            validator.validate(data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_CAMPAIGN_NAME.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidStatus() {
        try {
            when(data.getCompanyId()).thenReturn(1l);
            when(data.getName()).thenReturn("name");
            when(data.getStatus()).thenReturn("aaa");

            validator.validate(data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_CAMPAIGN_STATUS.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidCampaignId() {
        try {
            when(data.getCompanyId()).thenReturn(1l);
            when(data.getName()).thenReturn("name");
            when(data.getStatus()).thenReturn(ActiveStatus.ACTIVE.toString());
            when(data.getId()).thenReturn(1l);
            when(campaignService.findById(anyLong())).thenReturn(null);
            validator.validate(data);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_CAMPAIGN_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_success() {
        when(data.getCompanyId()).thenReturn(1l);
        when(data.getName()).thenReturn("name");
        when(data.getStatus()).thenReturn(ActiveStatus.INACTIVE.toString());
        when(data.getId()).thenReturn(null);
        validator.validate(data);
    }
}
