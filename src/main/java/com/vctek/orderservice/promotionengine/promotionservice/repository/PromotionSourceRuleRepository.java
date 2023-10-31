package com.vctek.orderservice.promotionengine.promotionservice.repository;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface PromotionSourceRuleRepository extends JpaRepository<PromotionSourceRuleModel, Long>, JpaSpecificationExecutor {
    PromotionSourceRuleModel findByCode(String code);

    List<PromotionSourceRuleModel> findAllByAppliedOnlyOneAndCodeIn(boolean appliedOnlyPromotion, List<String> fireRuleCodes);

    @Transactional
    @Modifying
    @Query(value = "UPDATE promotion_source_rule as ps JOIN drools_rule as dr ON ps.id = dr.promotion_source_rule_id " +
            " JOIN drools_kie_base as dkb ON dr.drools_kie_base_id = dkb.id " +
            " JOIN drools_kie_module as dkm ON dkm.drools_kie_base_id = dkb.id " +
            " SET status = ?3 WHERE dkm.name = ?2 AND ps.active = 1 AND (ps.end_date is null or ps.end_date >= ?1)",
            nativeQuery = true)
    int updateAllActiveRuleStatus(Date date, String moduleName, String status);



    @Transactional
    @Modifying
    @Query(value = "UPDATE promotion_source_rule as ps JOIN drools_rule as dr ON ps.id = dr.promotion_source_rule_id " +
            " JOIN drools_kie_base as dkb ON dr.drools_kie_base_id = dkb.id " +
            " JOIN drools_kie_module as dkm ON dkm.drools_kie_base_id = dkb.id " +
            " SET status = ?3 WHERE dkm.name = ?2 AND ps.status = ?4 AND " +
            " (ps.active = 0 OR (ps.end_date is not null AND ps.end_date < ?1))",
            nativeQuery = true)
    int updateAllExpiredRuleToInActive(Date date, String moduleName, String status, String currentStatus);

    PromotionSourceRuleModel findByIdAndCompanyId(Long id, Long companyId);

    Page<PromotionSourceRuleModel> findAllByCompanyId(Long companyId, Pageable pageable);

    List<PromotionSourceRuleModel> findAllByIdIn(List<Long> ids);

    @Query(value = "SELECT * FROM promotion_source_rule WHERE company_id = ?1 and active = true and (end_date is null or end_date >= ?2) ORDER BY ID DESC", nativeQuery = true)
    List<PromotionSourceRuleModel> findAllActiveOf(Long companyId, Date date);
}
