package com.vctek.orderservice.facade;

import com.vctek.migration.dto.MigrateOrderHistoryDto;
import com.vctek.orderservice.dto.EmployeeChangeData;

public interface OrderHistoryFacade {
    EmployeeChangeData getStatusHistory(String orderCode, Long companyId);

    void migrateOrderHistory(MigrateOrderHistoryDto migrateOrderHistoryDto);
}
