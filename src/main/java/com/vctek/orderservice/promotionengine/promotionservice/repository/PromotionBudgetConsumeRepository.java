package com.vctek.orderservice.promotionengine.promotionservice.repository;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionBudgetConsumeModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.mapper.ConsumeBudgetMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface PromotionBudgetConsumeRepository extends JpaRepository<PromotionBudgetConsumeModel, Long> {

    @Query(value = "SELECT SUM(pbc.discount_amount) FROM promotion_budget_consume as pbc " +
            " JOIN orders as o on o.code = pbc.order_code " +
            " LEFT JOIN return_order as ro on ro.exchange_order_id = o.id " +
            " WHERE pbc.customer_id = ?1 AND pbc.promotion_source_rule_id = ?2 AND pbc.month = ?3 AND pbc.year = ?4 " +
            " AND (o.is_exchange = false OR ro.id is not null) " +
            " AND (o.order_type != 'ONLINE' OR o.order_status NOT IN ('ORDER_RETURN', 'CUSTOMER_CANCEL', 'SYSTEM_CANCEL') OR o.order_status is null )" +
            " GROUP BY pbc.customer_id", nativeQuery = true)
    BigDecimal sumAllBudgetConsume(Long customerId, Long promotionSourceRuleId, int month, int year);


    @Query(value = "SELECT pbc.promotion_source_rule_id as sourceRuleId, SUM(pbc.discount_amount) as totalDiscountAmount " +
            " FROM promotion_budget_consume as pbc " +
            " JOIN orders as o on o.code = pbc.order_code " +
            " LEFT JOIN return_order as ro on ro.exchange_order_id = o.id " +
            " WHERE pbc.customer_id = ?1 AND pbc.promotion_source_rule_id IN (?2) AND pbc.month = ?3 AND pbc.year = ?4 " +
            " AND (o.is_exchange = false OR ro.id is not null) " +
            " AND (o.order_type != 'ONLINE' OR o.order_status NOT IN ('ORDER_RETURN', 'CUSTOMER_CANCEL', 'SYSTEM_CANCEL') OR o.order_status is null ) " +
            " GROUP BY pbc.promotion_source_rule_id", nativeQuery = true)
    List<ConsumeBudgetMapper> sumByPromotionSourceRulesOf(Long customerId, Set<Long> promotionSourceRuleIds, int month, int year);

}
