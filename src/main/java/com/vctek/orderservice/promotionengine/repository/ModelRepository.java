package com.vctek.orderservice.promotionengine.repository;

import com.vctek.orderservice.model.ItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository<T extends ItemModel> extends JpaRepository<T, Long> {
}
