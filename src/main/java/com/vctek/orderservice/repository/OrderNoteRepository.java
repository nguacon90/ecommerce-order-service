package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderNoteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderNoteRepository extends JpaRepository<OrderNoteModel, Long> {
    @Query(value = "SELECT orn.* FROM order_note as orn JOIN orders as o ON o.id = orn.order_id " +
            " WHERE o.code = ?1", nativeQuery = true)
    List<OrderNoteModel> findAllByOrderCode(String orderCode);
}
