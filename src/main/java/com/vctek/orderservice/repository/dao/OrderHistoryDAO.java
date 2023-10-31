package com.vctek.orderservice.repository.dao;

import com.vctek.migration.dto.MigrateOrderHistoryDto;
import com.vctek.orderservice.model.OrderHistoryModel;

public interface OrderHistoryDAO {
    void updateAuditing(OrderHistoryModel model, MigrateOrderHistoryDto dto);
}
