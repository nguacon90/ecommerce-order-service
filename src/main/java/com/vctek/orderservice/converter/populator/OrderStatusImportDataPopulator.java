package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderStatusImportData;
import com.vctek.orderservice.dto.OrderStatusImportDetailData;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderStatusImportDataPopulator implements Populator<OrderStatusImportModel, OrderStatusImportData> {

    @Override
    public void populate(OrderStatusImportModel source, OrderStatusImportData target) {
        target.setId(source.getId());
        target.setCompanyId(source.getCompanyId());
        target.setOrderStatus(source.getOrderStatus());
        target.setStatus(source.getStatus());
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedTime(source.getCreatedTime());
        if (CollectionUtils.isNotEmpty(source.getOrderStatusImportDetailModels())) {
            List<OrderStatusImportDetailData> detailDataList = new ArrayList<>();
            for (OrderStatusImportDetailModel detailModel : source.getOrderStatusImportDetailModels()) {
                OrderStatusImportDetailData data = new OrderStatusImportDetailData();
                data.setId(detailModel.getId());
                data.setOrderCode(detailModel.getOrderCode());
                data.setOldOrderStatus(detailModel.getOldOrderStatus());
                data.setNewOrderStatus(detailModel.getNewOrderStatus());
                data.setStatus(detailModel.getStatus());
                data.setNote(detailModel.getNote());
                detailDataList.add(data);
            }
            target.setDetails(detailDataList);
        }

    }
}
