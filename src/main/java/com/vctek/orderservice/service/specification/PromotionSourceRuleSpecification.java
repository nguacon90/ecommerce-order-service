package com.vctek.orderservice.service.specification;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionSourceRuleSpecification implements Specification<PromotionSourceRuleModel> {
    private PromotionRuleSearchParam searchParam;

    public PromotionSourceRuleSpecification(PromotionRuleSearchParam searchParam) {
        this.searchParam = searchParam;
    }

    @Override
    public Predicate toPredicate(Root<PromotionSourceRuleModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Long companyId = searchParam.getCompanyId();
        if(companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        predicates.add(cb.equal(root.get("companyId"), companyId));

        if(searchParam.getActive() != null) {
            predicates.add(cb.equal(root.get("active"), searchParam.getActive()));
        }

        if(StringUtils.isNotBlank(searchParam.getPublishedStatus())) {
            predicates.add(cb.equal(root.get("status"), searchParam.getPublishedStatus()));
        }

        if(searchParam.getCurrentUserId() != null) {
            predicates.add(cb.equal(root.get("createdBy"), searchParam.getCurrentUserId()));
        }

        if(searchParam.getCampaignId() != null) {
            Join<PromotionSourceRuleModel, CampaignModel> campaignsJoin = root.join("campaigns");
            predicates.add(cb.equal(campaignsJoin.get("id"), searchParam.getCampaignId()));
        }
        query.orderBy(cb.desc(root.get("id")));
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
