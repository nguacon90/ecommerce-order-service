package com.vctek.orderservice.promotionengine.promotionservice.repository;


import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractPromotionActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbstractPromotionActionRepository extends JpaRepository<AbstractPromotionActionModel, Long> {
    List<AbstractPromotionActionModel> findAllByPromotionResult(PromotionResultModel promotionResult);

    List<AbstractPromotionActionModel> findAllByGuidIn(List<String> guids);
}
