package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.kafka.data.ReturnOrdersDTO;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.ReturnOrdersKafkaOutStream;
import com.vctek.orderservice.kafka.producer.ReturnOrdersProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.BillService;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ReturnOrdersProducerServiceImpl implements ReturnOrdersProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateReturnOrderProducerImpl.class);
    private KafkaProducerService kafkaProducerService;
    private ReturnOrdersKafkaOutStream returnOrdersKafkaOutStream;
    private BillService billService;
    private ReturnOrderService returnOrderService;
    private Populator<OrderModel, OrderData> orderDataPopulator;

    @Autowired
    public ReturnOrdersProducerServiceImpl(@Qualifier("originOrderPopulator") Populator<OrderModel, OrderData> orderDataPopulator) {
        this.orderDataPopulator = orderDataPopulator;
    }

    @Override
    public void sendReturnOrdersKafka(ReturnOrderModel returnOrder) {
        if (returnOrder == null) {
            return;
        }
        OrderModel originOrder = returnOrder.getOriginOrder();
        if (originOrder == null) {
            LOGGER.info("Return order: {} without origin order", returnOrder.getId());
            return;
        }

        Long companyId = returnOrder.getCompanyId();
        Long billId = returnOrder.getBillId();
        Long returnOrderId = returnOrder.getId();
        try {
            ReturnOrderBillDTO returnOrderBill = billService.getBillWithReturnOrder(billId, companyId, returnOrderId);
            if (returnOrderBill == null) {
                LOGGER.warn("Cannot find return order bill for BillId: {}, companyId: {}, returnOrderId: {}",
                        billId, companyId, returnOrderId);
                return;
            }
            KafkaMessage<ReturnOrdersDTO> message = populateReturnOrdersDTOKafkaMessage(returnOrder, originOrder, returnOrderBill);
            kafkaProducerService.send(message, returnOrdersKafkaOutStream.produceReturnOrdersTopic());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private KafkaMessage<ReturnOrdersDTO> populateReturnOrdersDTOKafkaMessage(ReturnOrderModel returnOrder, OrderModel originOrder, ReturnOrderBillDTO returnOrderBill) {
        ReturnOrdersDTO returnOrdersDTO = new ReturnOrdersDTO();
        returnOrdersDTO.setId(returnOrder.getId());
        returnOrdersDTO.setOriginOrderCode(originOrder.getCode());
        returnOrdersDTO.setOriginOrderType(originOrder.getType());
        returnOrdersDTO.setCompanyId(returnOrder.getCompanyId());
        returnOrdersDTO.setWarehouseId(returnOrderBill.getWarehouseId());
        returnOrdersDTO.setEmployeeId(returnOrder.getCreatedBy());
        returnOrdersDTO.setModifiedBy(returnOrder.getModifiedBy());
        returnOrdersDTO.setModifiedTime(returnOrder.getModifiedTime());
        returnOrdersDTO.setReturnOrderBillDTO(returnOrderBill);
        returnOrdersDTO.setCreatedTime(returnOrder.getCreatedTime());
        returnOrdersDTO.setCreatedBy(returnOrder.getCreatedBy());
        returnOrdersDTO.setDeliveryFee(CommonUtils.readValue(returnOrder.getShippingFee()));
        returnOrdersDTO.setCollaboratorShippingFee(CommonUtils.readValue(returnOrder.getCollaboratorShippingFee()));
        returnOrdersDTO.setCompanyShippingFee(CommonUtils.readValue(returnOrder.getCompanyShippingFee()));
        returnOrdersDTO.setVat(CommonUtils.readValue(returnOrder.getVat()));
        if (returnOrder.getExchangeOrder() != null) {
            populateOrderData(returnOrder.getExchangeOrder(), returnOrdersDTO);
        }

        KafkaMessage<ReturnOrdersDTO> message = new KafkaMessage<>();
        message.setContent(returnOrdersDTO);
        message.setType(KafkaMessageType.RETURN_ORDERS);
        return message;
    }

    @Override
    public void produceReturnOrderMessage(final ReturnOrderBillDTO returnOrderBillDTO) {
        ReturnOrderModel returnOrderModel = returnOrderService.findByIdAndCompanyId(returnOrderBillDTO.getReturnOrderId(), returnOrderBillDTO.getCompanyId());
        if (returnOrderModel == null) {
            return;
        }
        OrderModel originOrder = returnOrderModel.getOriginOrder();
        if (originOrder == null) {
            LOGGER.info("Return order: {} without origin order", returnOrderModel.getId());
            return;
        }
        try {
            KafkaMessage<ReturnOrdersDTO> message = populateReturnOrdersDTOKafkaMessage(returnOrderModel, originOrder, returnOrderBillDTO);
            kafkaProducerService.send(message, returnOrdersKafkaOutStream.produceReturnOrdersTopic());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void populateOrderData(OrderModel originOrder, ReturnOrdersDTO returnOrdersDTO) {
        OrderData orderData = new OrderData();
        orderDataPopulator.populate(originOrder, orderData);
        returnOrdersDTO.setOrderData(orderData);
    }


    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    public void setKafkaProducerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Autowired
    public void setReturnOrdersKafkaOutStream(ReturnOrdersKafkaOutStream returnOrdersKafkaOutStream) {
        this.returnOrdersKafkaOutStream = returnOrdersKafkaOutStream;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }
}
