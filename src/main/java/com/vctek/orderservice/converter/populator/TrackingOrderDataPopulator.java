package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.data.OrderEntryData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.dto.TrackingOrderDetailData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrackingOrderDataPopulator implements Populator<OrderData, TrackingOrderData> {

    @Override
    public void populate(OrderData source, TrackingOrderData target) {
        target.setOrderCode(source.getOrderCode());
        target.setCompanyId(source.getCompanyId());
        target.setShippingCompanyId(source.getShippingCompanyId());
        target.setOrderSourceId(source.getOrderSourceId());
        target.setVat(source.getOrderVat());
        target.setVatType(source.getOrderVatType());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
        target.setDeliveryCost(source.getDeliveryFee());
        target.setCompanyShippingFee(source.getCompanyShippingFee());
        target.setCollaboratorShippingFee(source.getCollaboratorShippingFee());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setCustomerNote(source.getCustomerNote());
        target.setCustomerSupportNote(source.getCustomerSupportNote());
        target.setAddressDto(source.getAddressDto());
        if (source.getDimCustomerData() != null) {
            target.setCustomerId(source.getDimCustomerData().getCustomerId());
        }
        populateEntries(source, target);
    }

    private void populateEntries(OrderData source, TrackingOrderData target) {
        List<TrackingOrderDetailData> dataList = new ArrayList<>();
        for (OrderEntryData entryData : source.getEntryDataList()) {
            TrackingOrderDetailData data = new TrackingOrderDetailData();
            data.setEntryId(entryData.getOrderEntryId());
            data.setSubOrderEntryId(entryData.getSubOrderEntryId());
            data.setToppingOptionId(entryData.getToppingOptionId());
            data.setProductId(entryData.getProductId());
            data.setComboId(entryData.getComboId());
            data.setQuantity(entryData.getQuantity());
            data.setPrice(entryData.getPrice());
            data.setDiscount(entryData.getDiscount());
            data.setDiscountType(entryData.getDiscountType());
            dataList.add(data);
        }
        target.setDetails(dataList);
    }
}
