package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionModel, Long> {
    List<PaymentTransactionModel> findAllByOrderCode(String orderCode);

    List<PaymentTransactionModel> findAllByReturnOrder(ReturnOrderModel returnOrderModel);

    Page<PaymentTransactionModel> findAllByInvoiceIdIsNotNull(Pageable pageable);

    List<PaymentTransactionModel> findByMoneySourceIdAndPaymentMethodIdAndOrderCode(Long moneySourceId, Long paymentMethodId, String orderCode);

    @Query(value = "SELECT * FROM payment_transaction as pt JOIN return_order as r " +
            " ON pt.return_order_id = r.id WHERE pt.money_source_id = ?1 and" +
            " pt.payment_method_id = ?2 and r.external_id = ?3 and r.company_id = ?4 limit 1", nativeQuery = true)
    PaymentTransactionModel findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId(Long moneySourceId, Long paymentMethodId, Long returnExternalId, Long companyId);

    List<PaymentTransactionModel> findAllByOrderModelAndInvoiceId(OrderModel order, Long invoiceId);
}
