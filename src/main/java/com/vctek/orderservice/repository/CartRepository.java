package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.CartModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends AbstractOrderRepository<CartModel>, JpaRepository<CartModel, Long> {

    List<CartModel> findAllByCreateByUserAndCompanyIdAndType(Long userId, Long companyId, String orderType);

    CartModel findByCodeAndCompanyIdAndTypeAndCreateByUser(String code, Long companyId, String type, Long userId);

    CartModel findByCodeAndCreateByUserAndCompanyId(String code, Long userId, Long companyId);

    List<CartModel> findAllByCreateByUserAndTypeAndExchangeAndCompanyIdAndSellSignal(Long userId, String orderType, boolean exchangeCart, Long companyId,
                                                                                     String sellSignal);

    CartModel findByCodeAndCompanyId(String code, Long companyId);

    @Query(value = "FROM CartModel as c WHERE c.exchange = true")
    List<CartModel> findAllExchangeCarts();

    CartModel findByCode(String code);


    List<CartModel> findAllByCompanyIdAndGuid(Long companyId, String guid);

    List<CartModel> findAllByCompanyIdAndCreateByUserAndSellSignalAndDeleted(Long companyId, Long userId, String sellSignal, boolean deleted);

}
