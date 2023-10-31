package com.vctek.orderservice.facade;

import com.vctek.migration.dto.MigrateBillDto;

public interface SyncOrderFacade {

    void processSyncOrderMessage(MigrateBillDto data);
}
