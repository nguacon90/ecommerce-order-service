package com.vctek.orderservice.service.specification;

import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class ReturnOrderSpecification implements Specification<ReturnOrderModel> {
    private ReturnOrderSearchRequest searchParam;

    public ReturnOrderSpecification(ReturnOrderSearchRequest searchParam) {
        this.searchParam = searchParam;
    }

    @Override
    public Predicate toPredicate(Root<ReturnOrderModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Long companyId = searchParam.getCompanyId();
        if(companyId != null) {
            predicates.add(cb.equal(root.get("companyId"), companyId));
        }

        if(searchParam.getFromCreatedTime() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), searchParam.getFromCreatedTime()));
        }

        query.orderBy(cb.desc(root.get("id")));
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
