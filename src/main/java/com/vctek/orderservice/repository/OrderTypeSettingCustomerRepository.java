package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTypeSettingCustomerRepository extends JpaRepository<OrderTypeSettingCustomerModel, Long> {

}
