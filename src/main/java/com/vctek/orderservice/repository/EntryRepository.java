package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EntryRepository extends AbstractOrderEntryRepository<AbstractOrderEntryModel>, JpaRepository<AbstractOrderEntryModel, Long> {
    List<AbstractOrderEntryModel> findAllByOrder(AbstractOrderModel abstractOrderModel);

    List<AbstractOrderEntryModel> findAllByOrderAndSaleOffAndProductId(AbstractOrderModel order, boolean saleOff, Long productId);

    AbstractOrderEntryModel findByIdAndOrder(Long entryId, AbstractOrderModel order);

    @Query(value = "SELECT oe.* FROM order_entry AS oe JOIN topping_option as topt on oe.id = topt.order_entry_id WHERE oe.order_id = ?1", nativeQuery = true)
    List<AbstractOrderEntryModel> findAllEntryHasToppingOf(Long orderId);
}
