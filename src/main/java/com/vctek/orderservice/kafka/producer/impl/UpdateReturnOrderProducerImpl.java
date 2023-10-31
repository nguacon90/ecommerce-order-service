package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.*;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.OrderKafkaOutStream;
import com.vctek.kafka.stream.UpdateReturnOrderOutStream;
import com.vctek.orderservice.feignclient.dto.BillDetailData;
import com.vctek.orderservice.feignclient.dto.ReturnOrderBillData;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.kafka.producer.UpdateReturnOrderProducer;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.BillService;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.ReturnOrderCodeGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UpdateReturnOrderProducerImpl implements UpdateReturnOrderProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateReturnOrderProducerImpl.class);
    private KafkaProducerService kafkaProducerService;
    private OrderKafkaOutStream orderKafkaOutStream;
    private BillService billService;
    private ReturnOrderService returnOrderService;
    private OrderProducerService orderProducerService;
    private UpdateReturnOrderOutStream updateReturnOrderOutStream;
    private OrderEntryRepository orderEntryRepository;

    public UpdateReturnOrderProducerImpl(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Override
    public void process(ReturnOrderModel returnOrder) {
        if (returnOrder == null) {
            return;
        }
        OrderModel originOrder = returnOrderService.getOriginOrderOf(returnOrder);
        if (originOrder == null) {
            LOGGER.info("Return order: {} without origin order", returnOrder.getId());
            return;
        }

        OrderData orderData = new OrderData();
        Long returnOrderId = returnOrder.getId();
        Long companyId = returnOrder.getCompanyId();
        Long billId = returnOrder.getBillId();
        try {
            ReturnOrderBillData returnOrderBill = billService.getReturnOrderBill(billId, companyId, returnOrderId);
            if (returnOrderBill == null) {
                LOGGER.warn("Cannot find return order bill for BillId: {}, companyId: {}, returnOrderId: {}",
                        billId, companyId, returnOrderId);
                return;
            }
            sendKafkaWithExchangeOrder(returnOrder);
            orderData.setOrderCode(ReturnOrderCodeGenerator.generate(returnOrderId, billId));
            orderData.setCreatedDate(returnOrder.getCreatedTime());
            orderData.setOriginOrderCode(returnOrder.getOriginOrder().getCode());
            orderData.setOrderType(getReturnOrderType(originOrder));
            OrderSourceModel orderSourceModel = originOrder.getOrderSourceModel();
            if (orderSourceModel != null) {
                orderData.setOrderSourceId(orderSourceModel.getId());
                orderData.setOrderSourceName(orderSourceModel.getName());
            }
            orderData.setCompanyId(companyId);
            orderData.setWarehouseId(returnOrderBill.getWarehouseId());
            orderData.setOrderDeleted(false);
            orderData.setOrderStatus(OrderStatus.COMPLETED.code());
            orderData.setVat(returnOrder.getVat());
            orderData.setFinalPrice(CommonUtils.readValue(returnOrderBill.getFinalPrice()) +
                    CommonUtils.readValue(returnOrder.getShippingFee()) + CommonUtils.readValue(returnOrder.getVat()));
            orderData.setTotalPrice(returnOrderBill.getFinalPrice());
            orderData.setTotalDiscount(returnOrderBill.getTotalDiscount());
            orderData.setFinalDiscount(returnOrderBill.getTotalDiscount());
            orderData.setAge(originOrder.getAge());
            orderData.setGender(originOrder.getGender());
            orderData.setEmployeeId(returnOrder.getCreatedBy());
            orderData.setModifiedTime(returnOrder.getModifiedTime());
            orderData.setModifiedBy(returnOrder.getModifiedBy());
            orderData.setDeliveryFee(returnOrder.getShippingFee());
            orderData.setCollaboratorShippingFee(returnOrder.getCollaboratorShippingFee());
            orderData.setCompanyShippingFee(returnOrder.getCompanyShippingFee());
            List<OrderEntryData> entryData = populateEntry(orderData, returnOrderBill, returnOrder.getOriginOrder());
            orderData.setEntryDataList(entryData);

            populateCustomer(orderData, originOrder);
            populateSettingCustomerOption(orderData, originOrder);

            KafkaMessage<OrderData> message = new KafkaMessage<>();
            message.setContent(orderData);
            message.setType(KafkaMessageType.ORDERS);
            kafkaProducerService.send(message, orderKafkaOutStream.produceOrderTopic());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void populateSettingCustomerOption(OrderData orderData, OrderModel orderModel) {
        List<OrderSettingCustomerOptionData> orderSettingCustomerOptions = new ArrayList<>();
        for (OrderSettingCustomerOptionModel orderSettingCustomerOptionModel : orderModel.getOrderSettingCustomerOptionModels()) {
            OrderSettingCustomerOptionData orderSettingCustomerOptionData = new OrderSettingCustomerOptionData();
            orderSettingCustomerOptionData.setId(orderSettingCustomerOptionModel.getId());
            orderSettingCustomerOptionData.setOrderSettingCustomerId(orderSettingCustomerOptionModel.getOrderSettingCustomerModel().getId());
            orderSettingCustomerOptions.add(orderSettingCustomerOptionData);
        }
        orderData.setSettingCustomerOptions(orderSettingCustomerOptions);
    }

    @Override
    public void processReturnOrderBill(final ReturnOrderBillDTO returnOrderBillDTO) {
        ReturnOrderModel returnOrder = returnOrderService.findByIdAndCompanyId(returnOrderBillDTO.getReturnOrderId(),
                returnOrderBillDTO.getCompanyId());
        if (returnOrder == null) {
            return;
        }

        OrderModel originOrder = returnOrderService.getOriginOrderOf(returnOrder);
        if (originOrder == null) {
            LOGGER.info("Return order: {} without origin order", returnOrder.getId());
            return;
        }

        sendKafkaWithExchangeOrder(returnOrder);
        OrderData orderData = new OrderData();
        Long returnOrderId = returnOrder.getId();
        Long companyId = returnOrder.getCompanyId();
        Long billId = returnOrder.getBillId();
        orderData.setOrderCode(ReturnOrderCodeGenerator.generate(returnOrderId, billId));
        orderData.setCreatedDate(returnOrder.getCreatedTime());
        orderData.setOrderType(getReturnOrderType(originOrder));
        orderData.setCompanyId(companyId);
        orderData.setWarehouseId(returnOrderBillDTO.getWarehouseId());
        orderData.setOrderDeleted(false);
        orderData.setOrderStatus(OrderStatus.COMPLETED.code());
        orderData.setVat(returnOrder.getVat());
        orderData.setFinalPrice(returnOrderBillDTO.getFinalPrice() + returnOrder.getCollaboratorShippingFee()
                + returnOrder.getCompanyShippingFee() + returnOrder.getShippingFee() + returnOrder.getVat());
        orderData.setTotalPrice(returnOrderBillDTO.getFinalPrice());
        orderData.setTotalDiscount(returnOrderBillDTO.getTotalDiscount());
        orderData.setFinalDiscount(returnOrderBillDTO.getTotalDiscount());
        orderData.setEmployeeId(returnOrder.getCreatedBy());
        orderData.setModifiedTime(returnOrder.getModifiedTime());
        orderData.setModifiedBy(returnOrder.getModifiedBy());
        orderData.setDeliveryFee(returnOrder.getShippingFee());
        orderData.setCollaboratorShippingFee(returnOrder.getCollaboratorShippingFee());
        orderData.setCompanyShippingFee(returnOrder.getCompanyShippingFee());
        orderData.setOriginOrderCode(returnOrder.getOriginOrder().getCode());
        orderData.setAge(originOrder.getAge());
        orderData.setGender(originOrder.getGender());
        OrderSourceModel orderSourceModel = originOrder.getOrderSourceModel();
        if (orderSourceModel != null) {
            orderData.setOrderSourceId(orderSourceModel.getId());
            orderData.setOrderSourceName(orderSourceModel.getName());
        }
        List<OrderEntryData> entryData = populateEntry(orderData, returnOrderBillDTO, returnOrder.getOriginOrder());
        orderData.setEntryDataList(entryData);

        populateCustomer(orderData, originOrder);

        KafkaMessage<OrderData> message = new KafkaMessage<>();
        message.setContent(orderData);
        message.setType(KafkaMessageType.ORDERS);
        kafkaProducerService.send(message, orderKafkaOutStream.produceOrderTopic());
    }

    @Override
    public void sendRequestUpdateReturnOrder(ReturnOrderModel model) {
        ReturnOrderBillDTO dto = new ReturnOrderBillDTO();
        dto.setId(model.getBillId());
        dto.setCompanyId(model.getCompanyId());
        dto.setReturnOrderId(model.getId());

        KafkaMessage<ReturnOrderBillDTO> message = new KafkaMessage<>();
        message.setContent(dto);
        message.setType(KafkaMessageType.REQUEST_UPDATE_RETURN_ORDER);
        kafkaProducerService.send(message, updateReturnOrderOutStream.produceReturnOrderTopic());
    }

    private void sendKafkaWithExchangeOrder(ReturnOrderModel returnOrder) {
        OrderModel exchangeOrder = returnOrder.getExchangeOrder();
        if (exchangeOrder != null) {
            OrderEvent event = new OrderEvent(exchangeOrder);
            orderProducerService.sendOrderKafka(event);
        }
    }

    private List<OrderEntryData> populateEntry(OrderData dimOrderData, ReturnOrderBillDTO returnOrderBill, OrderModel originOrder) {
        List<OrderEntryData> entryData = new ArrayList<>();
        List<ReturnOrdersBillDetailDTO> returnOrderBillEntries = returnOrderBill.getEntries();
        long quantity = 0;
        if (CollectionUtils.isNotEmpty(returnOrderBillEntries)) {
            Map<Long, OrderEntryModel> entryModelMapGiveAway = new HashMap<>();
                    List<OrderEntryModel> orderEntryModels = orderEntryRepository.findAllByOrder(originOrder);
            if (CollectionUtils.isNotEmpty(orderEntryModels)) {
                entryModelMapGiveAway = orderEntryModels.stream().filter(i -> i.isGiveAway())
                        .collect(Collectors.toMap(i -> i.getId(), i -> i));
            }

            OrderEntryData entry;
            for (ReturnOrdersBillDetailDTO returnOrderBillDetailDTO : returnOrderBillEntries) {
                entry = new OrderEntryData();
                entry.setProductId(returnOrderBillDetailDTO.getProductId());
                Integer orderEntryId = returnOrderBillDetailDTO.getOrderEntryId() != null ? returnOrderBillDetailDTO.getOrderEntryId().intValue() : null;
                entry.setOrderEntryId(returnOrderBillDetailDTO.getOrderEntryId());
                entry.setOrderEntryNumber(orderEntryId);
                entry.setPrice(returnOrderBillDetailDTO.getPrice());
                entry.setQuantity(returnOrderBillDetailDTO.getQuantity());
                entry.setOrderEntryTotalDiscount(returnOrderBillDetailDTO.getDiscountValue());
                entry.setOrderEntryFixDiscount(returnOrderBillDetailDTO.getDiscountValue());
                entry.setOrderEntryTotalPrice(returnOrderBillDetailDTO.getTotalPrice());
                entry.setOriginBasePrice(returnOrderBillDetailDTO.getOriginBasePrice());
                entry.setComboId(returnOrderBillDetailDTO.getComboId());
                entry.setComboQuantity(returnOrderBillDetailDTO.getComboQty());
                quantity += returnOrderBillDetailDTO.getQuantity() == null ? 0 : returnOrderBillDetailDTO.getQuantity();
                if (entryModelMapGiveAway.containsKey(returnOrderBillDetailDTO.getOrderEntryId())) {
                    entry.setGiveAway(true);
                }
                entryData.add(entry);
            }
        }
        dimOrderData.setTotalQuantity(quantity);
        dimOrderData.setTotalProduct(entryData.size());
        return entryData;
    }

    private String getReturnOrderType(OrderModel originOrder) {
        if (OrderType.ONLINE.toString().equals(originOrder.getType())) {
            return OrderType.RETURN_ORDER_ONLINE.toString();
        }

        if (OrderType.RETAIL.toString().equals(originOrder.getType())) {
            return OrderType.RETURN_ORDER_RETAIL.toString();
        }

        if (OrderType.WHOLESALE.toString().equals(originOrder.getType())) {
            return OrderType.RETURN_ORDER_WHOLESALE.toString();
        }

        return OrderType.RETURN_ORDER.toString();
    }

    private void populateCustomer(OrderData dimOrderData, OrderModel originOrder) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerId(originOrder.getCustomerId());
        dimOrderData.setDimCustomerData(customerDto);
    }

    private List<OrderEntryData> populateEntry(OrderData dimOrderData, ReturnOrderBillData returnOrderBill, OrderModel originOrder) {
        List<OrderEntryData> entryData = new ArrayList<>();
        List<BillDetailData> returnOrderBillEntries = returnOrderBill.getEntries();
        Map<Long, AbstractOrderEntryModel> entryModelMapGiveAway = originOrder.getEntries().stream().filter(i -> i.isGiveAway())
                .collect(Collectors.toMap(i -> i.getId(), i -> i));
        long quantity = 0;
        if (CollectionUtils.isNotEmpty(returnOrderBillEntries)) {
            OrderEntryData entry;
            for (BillDetailData billDetail : returnOrderBillEntries) {
                entry = new OrderEntryData();
                entry.setProductId(billDetail.getProductId());
                Integer orderEntryId = billDetail.getOrderEntryId() != null ? billDetail.getOrderEntryId().intValue() : null;
                entry.setOrderEntryNumber(orderEntryId);
                entry.setOrderEntryId(billDetail.getOrderEntryId());
                entry.setPrice(billDetail.getPrice());
                entry.setQuantity(billDetail.getQuantity());
                entry.setOrderEntryTotalDiscount(billDetail.getDiscountValue());
                entry.setOrderEntryFixDiscount(billDetail.getDiscountValue());
                entry.setOrderEntryTotalPrice(billDetail.getTotalPrice());
                entry.setComboId(billDetail.getComboId());
                entry.setComboQuantity(billDetail.getComboQuantity());
                entry.setOriginBasePrice(billDetail.getOriginBasePrice());
                quantity += billDetail.getQuantity() == null ? 0 : billDetail.getQuantity();
                if (entryModelMapGiveAway.containsKey(billDetail.getOrderEntryId())) {
                    entry.setGiveAway(true);
                }
                entryData.add(entry);
            }
        }
        dimOrderData.setTotalQuantity(quantity);
        dimOrderData.setTotalProduct(entryData.size());
        return entryData;
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
    public void setOrderKafkaOutStream(OrderKafkaOutStream orderKafkaOutStream) {
        this.orderKafkaOutStream = orderKafkaOutStream;
    }

    @Autowired
    public void setOrderProducerService(OrderProducerService orderProducerService) {
        this.orderProducerService = orderProducerService;
    }

    @Autowired
    public void setUpdateReturnOrderOutStream(UpdateReturnOrderOutStream updateReturnOrderOutStream) {
        this.updateReturnOrderOutStream = updateReturnOrderOutStream;
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }
}
