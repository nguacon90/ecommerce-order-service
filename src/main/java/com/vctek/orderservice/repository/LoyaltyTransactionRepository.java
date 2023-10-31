package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.LoyaltyTransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransactionModel, Long> {

    LoyaltyTransactionModel findByOrderCodeAndInvoiceNumber(String orderCode, String invoiceNumber);

    List<LoyaltyTransactionModel> findAllByOrderCode(String orderCode);

    @Query(value ="SELECT * FROM loyalty_transaction WHERE order_code =?1 Order By id DESC LIMIT 1",nativeQuery = true)
    Optional<LoyaltyTransactionModel> findLastByOrderCode(String orderCode);

    @Query(value ="SELECT * FROM loyalty_transaction WHERE order_code =?1 AND type in ?2 Order By id DESC LIMIT 1",nativeQuery = true)
    Optional<LoyaltyTransactionModel> findLastByOrderCodeAndListType(String orderCode,List<String> types);
}
