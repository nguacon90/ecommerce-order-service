package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.ToppingItemModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface ToppingItemRepository extends JpaRepository<ToppingItemModel, Long> {
    ToppingItemModel findByIdAndToppingOptionModel(Long id, ToppingOptionModel toppingOptionModel);

    @Query(value = "SELECT * FROM topping_item as tpi JOIN topping_option as tpo ON tpi.topping_option_id = tpo.id" +
            " JOIN order_entry as oe ON tpo.order_entry_id = oe.id" +
            " JOIN orders as od ON oe.order_id = od.id WHERE od.id = ?1", nativeQuery = true)
    Set<ToppingItemModel> findAllByOrderId(Long orderId);

    List<ToppingItemModel> findAllByToppingOptionModel(ToppingOptionModel toppingOptionModel);

    @Query(value = "SELECT distinct tpi.* FROM topping_item as tpi JOIN topping_option as tpo ON tpi.topping_option_id = tpo.id" +
            " JOIN order_entry as oe ON tpo.order_entry_id = oe.id WHERE oe.id = ?1", nativeQuery = true)
    Set<ToppingItemModel> findAllByEntryId(Long entryId);
}
