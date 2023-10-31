package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.request.OrderFileParameter;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.util.ExportExcelType;
import org.springframework.stereotype.Component;

@Component
public class OrderFileParameterPopulator implements Populator<OrderSearchRequest, OrderFileParameter> {

    @Override
    public void populate(OrderSearchRequest request, OrderFileParameter orderFileParameter) {
        orderFileParameter.setFileNum(request.getFileNum());
        orderFileParameter.setExportExcelType(ExportExcelType.findByCode(request.getExportType()));
        orderFileParameter.setUserId(request.getUserId());
        orderFileParameter.setOrderType(request.getOrderType());
    }
}
