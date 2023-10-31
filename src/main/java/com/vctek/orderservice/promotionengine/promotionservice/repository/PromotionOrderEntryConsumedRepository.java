package com.vctek.orderservice.promotionengine.promotionservice.repository;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionOrderEntryConsumedModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionOrderEntryConsumedRepository extends JpaRepository<PromotionOrderEntryConsumedModel, Long> {

    @Query(nativeQuery = true, value = "select sum(applied_quantity) from promotion_order_entry_consume where order_entry_id = ?1")
    Long sumQuantityByOrderEntry(Long orderEntryId);

    List<PromotionOrderEntryConsumedModel> findAllByPromotionResult(PromotionResultModel promotionResultModel);
}
