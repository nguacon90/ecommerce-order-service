package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.ProductInventoryData;
import com.vctek.kafka.data.ProductInventoryDetailData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.ProductInventoryOutStream;
import com.vctek.orderservice.kafka.producer.UpdateProductInventoryProducer;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.util.InventoryStatus;
import com.vctek.util.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateProductInventoryProducerImpl implements UpdateProductInventoryProducer {
    private KafkaProducerService kafkaProducerService;
    private ProductInventoryOutStream outStream;

    @Override
    public void sendUpdateStockEntries(OrderModel orderModel, List<AbstractOrderEntryModel> entryModels) {
        KafkaMessage<ProductInventoryData> message = new KafkaMessage<>();
        ProductInventoryData data = new ProductInventoryData();
        List<ProductInventoryDetailData> products = new ArrayList<>();
        data.setCompanyId(orderModel.getCompanyId());
        data.setOrderCode(orderModel.getCode());
        data.setOrderType(orderModel.getType());
        data.setUserId(orderModel.getModifiedBy());
        data.setWarehouseId(orderModel.getWarehouseId());
        data.setBillId(orderModel.getBillId());
        for (AbstractOrderEntryModel entryModel : entryModels) {
            ProductInventoryDetailData detailData = new ProductInventoryDetailData();
            detailData.setOrderEntryId(entryModel.getId());
            detailData.setProductId(entryModel.getProductId());
            detailData.setSaleOff(entryModel.isSaleOff());
            detailData.setQuantity(entryModel.getQuantity().intValue());
            populateInventoryStatusCode(entryModel, detailData);
            products.add(detailData);
        }
        data.setProducts(products);
        message.setContent(data);
        kafkaProducerService.send(message, outStream.produce());
    }

    private void populateInventoryStatusCode(AbstractOrderEntryModel entryModel, ProductInventoryDetailData detailData) {
        if (entryModel.isSaleOff()) {
            detailData.setFromCode(InventoryStatus.BROKEN.code());
            detailData.setToCode(InventoryStatus.AVAILABLE.code());
            return;
        }
        if (!entryModel.isSaleOff()) {
            detailData.setFromCode(InventoryStatus.AVAILABLE.code());
            detailData.setToCode(InventoryStatus.BROKEN.code());
        }
    }

    @Autowired
    public void setKafkaProducerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Autowired
    public void setOutStream(ProductInventoryOutStream outStream) {
        this.outStream = outStream;
    }
}
