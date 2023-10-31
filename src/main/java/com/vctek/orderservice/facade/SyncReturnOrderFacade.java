package com.vctek.orderservice.facade;

import com.vctek.migration.dto.MigrateBillDto;

public interface SyncReturnOrderFacade {

    void processSyncReturnOrderMessage(MigrateBillDto data);
}
