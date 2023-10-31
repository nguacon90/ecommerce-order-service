package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartEntryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartEntryRepository extends AbstractOrderEntryRepository<CartEntryModel>, JpaRepository<CartEntryModel, Long> {

    @Query(value = "SELECT SUM(quantity) FROM order_entry WHERE order_id = ?1", nativeQuery = true)
    Long sumTotalQuantity(Long orderId);

    List<CartEntryModel> findAllByOrderAndProductId(AbstractOrderModel order, Long productId);

    List<CartEntryModel> findAllByOrderAndProductIdAndWarehouseId(AbstractOrderModel order, Long productId,
                                                                  Long warehouseId);

    CartEntryModel findByOrderAndEntryNumber(AbstractOrderModel order, Integer entryNumber);

    CartEntryModel findByIdAndOrder(Long id, AbstractOrderModel order);

    List<CartEntryModel> findAllByOrder(AbstractOrderModel orderModel);
}
