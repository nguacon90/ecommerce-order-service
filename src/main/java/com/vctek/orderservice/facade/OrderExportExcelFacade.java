package com.vctek.orderservice.facade;

import com.vctek.dto.ExcelStatusData;
import com.vctek.orderservice.dto.request.OrderSearchRequest;

public interface OrderExportExcelFacade {

    boolean isExportExcel(OrderSearchRequest request);

    void processExportExcel(OrderSearchRequest request, boolean isProcessing);

    ExcelStatusData checkStatus(OrderSearchRequest request);
}
