package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.BasicData;
import com.vctek.kafka.data.OrderSettingCustomerData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.OrderSettingCustomerKafkaOutStream;
import com.vctek.kafka.stream.loyalty.LoyaltyInvoiceOutStream;
import com.vctek.orderservice.kafka.producer.OrderSettingCustomerProducerService;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderSettingCustomerProducerServiceImpl implements OrderSettingCustomerProducerService {
    private KafkaProducerService kafkaProducerService;
    private OrderSettingCustomerKafkaOutStream outStream;

    public OrderSettingCustomerProducerServiceImpl(KafkaProducerService kafkaProducerService,
                                                   OrderSettingCustomerKafkaOutStream outStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.outStream = outStream;
    }

    @Override
    public void createOrUpdateOrderSettingCustomer(OrderSettingCustomerModel model) {
        OrderSettingCustomerData data = populateData(model);
        KafkaMessage<OrderSettingCustomerData> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(data);
        kafkaProducerService.send(kafkaMessage, outStream.produceOrderTopic());
    }

    private OrderSettingCustomerData populateData(OrderSettingCustomerModel model) {
        OrderSettingCustomerData data = new OrderSettingCustomerData();
        data.setId(model.getId());
        data.setName(model.getName());
        data.setCompanyId(model.getCompanyId());
        data.setPriority(model.getPriority());
        data.setDeleted(model.isDeleted());
        if (CollectionUtils.isNotEmpty(model.getOrderTypeSettingCustomerModels())) {
            List<BasicData> basicDataList = new ArrayList<>();
            for (OrderSettingCustomerOptionModel optionModel : model.getOptionModels()) {
                BasicData basicData = new BasicData();
                basicData.setId(optionModel.getId());
                basicData.setName(optionModel.getName());
                basicData.setDeleted(optionModel.isDeleted());
                basicDataList.add(basicData);
            }
            data.setOptions(basicDataList);
        }
        return data;
    }
}
