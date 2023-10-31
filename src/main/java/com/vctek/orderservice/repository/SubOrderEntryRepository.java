package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubOrderEntryRepository extends JpaRepository<SubOrderEntryModel, Long> {
    SubOrderEntryModel findByOrderEntryAndId(AbstractOrderEntryModel entryModel, Long id);

    List<SubOrderEntryModel> findAllByOrderEntry(AbstractOrderEntryModel entryModel);


    @Query(value = "SELECT * FROM sub_order_entry as sub" +
            " JOIN order_entry as oe ON sub.order_entry_id = oe.id" +
            " JOIN orders as od ON oe.order_id = od.id WHERE od.id = ?1", nativeQuery = true)
    List<SubOrderEntryModel> findAllByOrderId(Long orderId);
}
