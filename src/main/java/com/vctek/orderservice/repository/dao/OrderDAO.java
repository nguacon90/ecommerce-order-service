package com.vctek.orderservice.repository.dao;

import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.storefront.CountOrderData;
import com.vctek.orderservice.model.OrderModel;

import java.util.List;

public interface OrderDAO {

    void updateAuditing(OrderModel model, MigrateBillDto dto);

    List<CountOrderData> storefrontCountOrderByUser(OrderSearchRequest request);
}
