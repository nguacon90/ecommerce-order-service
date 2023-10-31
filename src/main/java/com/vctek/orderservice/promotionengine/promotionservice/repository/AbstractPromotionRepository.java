package com.vctek.orderservice.promotionengine.promotionservice.repository;

import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractPromotionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractPromotionRepository<T extends AbstractPromotionModel>
        extends JpaRepository<T, Long> {
}
