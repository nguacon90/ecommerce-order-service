package com.vctek.orderservice.promotionengine.promotionservice.repository;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PromotionResultRepository extends JpaRepository<PromotionResultModel, Long> {

    @Query(value = "SELECT * FROM promotion_results WHERE order_id = ?1", nativeQuery = true)
    List<PromotionResultModel> findAllByOrderId(Long orderId);

    List<PromotionResultModel> findAllByOrder(AbstractOrderModel abstractOrderModel);

    @Query(nativeQuery = true, value = "select ps.* from promotion_results ps " +
            "join promotion_order_entry_consume poec on ps.id = poec.promotion_result_id " +
            "join order_entry oe on poec.order_entry_id = oe.id " +
            "where oe.id = ?1")
    List<PromotionResultModel> findAllByOrderEntryId(Long orderEntryId);

    @Query(nativeQuery = true, value = "select DISTINCT pr.* FROM promotion_results pr join promotion p2 on pr.promotion_id = p2.id " +
            "join drools_rule dr on dr.promotion_id = p2.id " +
            "join promotion_source_rule psr on psr.id = dr.promotion_source_rule_id " +
            "join coupon c2 on c2.promotion_source_rule_id = psr.id " +
            "where c2.id in ?1 and pr.order_code = ?2")
    List<PromotionResultModel> findAllByCouponIdInAndOrderCode(List<Long> couponIds, String orderCode);

}
