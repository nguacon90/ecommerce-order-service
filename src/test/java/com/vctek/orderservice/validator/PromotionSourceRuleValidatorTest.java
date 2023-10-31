package com.vctek.orderservice.validator;

import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PromotionSourceRuleValidatorTest {
    private PromotionSourceRuleValidator validator;
    private PromotionSourceRuleDTO dto;

    @Before
    public void setUp() {
        dto = new PromotionSourceRuleDTO();
        validator = new PromotionSourceRuleValidator();
    }

    @Test
    public void validate_emptyCompanyId() {
        try {
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyMessageFired() {
        try {
            dto.setCompanyId(1l);
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PROMOTION_MESSAGE_FIRED.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyConditions() {
        try {
            dto.setCompanyId(1l);
            dto.setMessageFired("message");
            dto.setCampaignId(1l);
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PROMOTION_CONDITIONS.code(), e.getCode());
        }
    }

    @Test
    public void validate_emptyActions() {
        try {
            dto.setCompanyId(1l);
            dto.setMessageFired("message");
            dto.setCampaignId(1l);
            dto.setConditions(Arrays.asList(new ConditionDTO()));
            validator.validate(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PROMOTION_ACTIONS.code(), e.getCode());
        }
    }

    @Test
    public void validate() {
        dto.setCompanyId(1l);
        dto.setMessageFired("message");
        dto.setCampaignId(1l);
        dto.setConditions(Arrays.asList(new ConditionDTO()));
        dto.setActions(Arrays.asList(new ActionDTO()));
        validator.validate(dto);
    }
}
