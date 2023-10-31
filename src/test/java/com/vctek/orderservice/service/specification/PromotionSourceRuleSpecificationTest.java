package com.vctek.orderservice.service.specification;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PromotionSourceRuleSpecificationTest {
    private PromotionSourceRuleSpecification specification;
    private PromotionRuleSearchParam param = new PromotionRuleSearchParam();

    @Mock
    private Root<PromotionSourceRuleModel> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Join<Object, Object> campaignsJoin;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        specification = new PromotionSourceRuleSpecification(param);
    }

    @Test
    public void toPredicate_emptyCompanyId() {
        try {
            specification.toPredicate(root, query, cb);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void searchFull() {
        param.setCompanyId(1l);
        param.setActive(true);
        param.setCampaignId(1l);
        when(root.join("campaigns")).thenReturn(campaignsJoin);

        specification.toPredicate(root, query, cb);
        verify(root).get("companyId");
        verify(root).get("active");
        verify(campaignsJoin).get("id");
    }
}
