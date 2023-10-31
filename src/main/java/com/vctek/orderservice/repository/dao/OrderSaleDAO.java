package com.vctek.orderservice.repository.dao;

import com.vctek.orderservice.dto.SaleQuantity;
import com.vctek.orderservice.dto.request.SaleQuantityRequest;

import java.util.List;

public interface OrderSaleDAO {
    List<SaleQuantity> findComboEntrySaleQuantity(SaleQuantityRequest request);

    List<SaleQuantity> findEntrySaleQuantity(SaleQuantityRequest request);
}
