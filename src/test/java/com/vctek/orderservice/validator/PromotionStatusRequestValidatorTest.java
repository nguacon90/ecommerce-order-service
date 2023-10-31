package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.PromotionStatusRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class PromotionStatusRequestValidatorTest {
    private PromotionStatusRequestValidator validator;
    @Mock
    private PromotionSourceRuleService service;
    @Mock
    private PromotionStatusRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new PromotionStatusRequestValidator();
        validator.setPromotionSourceRuleService(service);
    }

    @Test
    public void validate_emptyCompanyId() {
        when(request.getCompanyId()).thenReturn(null);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_invalidSourceRule() {
        when(request.getCompanyId()).thenReturn(1l);
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_SOURCE_RULE_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_valid() {
        when(request.getCompanyId()).thenReturn(1l);
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new PromotionSourceRuleModel());
        validator.validate(request);
    }
}
