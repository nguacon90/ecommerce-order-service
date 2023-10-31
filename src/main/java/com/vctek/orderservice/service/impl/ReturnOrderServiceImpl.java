package com.vctek.orderservice.service.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.LinkReturnOrderforbillRequest;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.kafka.producer.LoyaltyTransactionProducerService;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.kafka.producer.ReturnOrdersProducerService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.OrderRepository;
import com.vctek.orderservice.repository.ReturnOrderRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.service.specification.ReturnOrderSpecification;
import com.vctek.orderservice.strategy.impl.DefaultCommercePlaceOrderStrategy;
import com.vctek.orderservice.util.EventType;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.TransactionType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReturnOrderServiceImpl implements ReturnOrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnOrderServiceImpl.class);
    private ReturnOrderRepository returnOrderRepository;
    private OrderRepository orderRepository;
    private DefaultCommercePlaceOrderStrategy defaultCommercePlaceOrderStrategy;
    private BillService billService;
    private ReturnOrdersProducerService returnOrdersProducerService;
    private OrderProducerService producerService;
    private ApplicationEventPublisher applicationEventPublisher;
    private CalculationService calculationService;
    private LoyaltyTransactionService loyaltyTransactionService;
    private LoyaltyService loyaltyService;
    private OrderService orderService;
    private Converter<ReturnOrderBillDTO, UpdateReturnOrderBillDTO> updateReturnOrderEntriesConverter;
    private LoyaltyTransactionProducerService loyaltyUpdateTransactionProducer;
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;
    private Converter<OrderModel, OrderBillLinkDTO> orderBillLinkDTOConverter;

    @Override
    @Transactional
    public ReturnOrderModel create(ReturnOrderCommerceParameter parameter) {
        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        ReturnOrderRequest returnOrderRequest = parameter.getReturnOrderRequest();
        final OrderModel originOrder = parameter.getOriginOrder();
        returnOrderModel.setOriginOrder(originOrder);
        returnOrderModel.setCompanyId(originOrder.getCompanyId());
        returnOrderModel.setNote(parameter.getNote());
        returnOrderModel.setShippingFee(returnOrderRequest.getShippingFee());
        returnOrderModel.setCompanyShippingFee(returnOrderRequest.getCompanyShippingFee());
        returnOrderModel.setCollaboratorShippingFee(returnOrderRequest.getCollaboratorShippingFee());
        returnOrderModel.setVat(returnOrderRequest.getVat());

        CartModel exchangeCart = parameter.getExchangeCart();
        Set<PaymentTransactionModel> paymentTransactions = parameter.getPaymentTransactions();
        if (CollectionUtils.isNotEmpty(paymentTransactions)) {
            paymentTransactions.forEach(p -> {
                p.setReturnOrder(returnOrderModel);
            });
        }
        returnOrderModel.setPaymentTransactions(paymentTransactions);
        ReturnOrderModel savedReturnOrder = returnOrderRepository.save(returnOrderModel);
        populateRevertAndRefundAmount(parameter, savedReturnOrder);
        updateOriginOrder(originOrder, parameter.getReturnOrderRequest());
        populateBill(parameter, savedReturnOrder);
        if (exchangeCart != null) {
            CommerceCheckoutParameter commerceCheckoutParameter = new CommerceCheckoutParameter();
            commerceCheckoutParameter.setCart(exchangeCart);
            commerceCheckoutParameter.setCardNumber(exchangeCart.getCardNumber());
            commerceCheckoutParameter.setPaymentTransactions(parameter.getExchangePaymentTransactions());
            commerceCheckoutParameter.setEmployeeId(originOrder.getEmployeeId());
            commerceCheckoutParameter.setAge(originOrder.getAge());
            commerceCheckoutParameter.setGender(originOrder.getGender());
            if (originOrder.getOrderSourceModel() != null) {
                commerceCheckoutParameter.setOrderSourceId(originOrder.getOrderSourceModel().getId());
            }
            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest.setId(originOrder.getCustomerId());
            commerceCheckoutParameter.setCustomerRequest(customerRequest);
            CommerceOrderResult commerceOrderResult = defaultCommercePlaceOrderStrategy.placeOrder(commerceCheckoutParameter);
            OrderModel exchangeOrder = commerceOrderResult.getOrderModel();
            returnOrderModel.setExchangeOrder(exchangeOrder);
            returnOrderModel.setExchangeOrderCode(exchangeOrder.getCode());
        }
        OrderEvent orderEvent = new OrderEvent(originOrder);
        orderEvent.setEventType(EventType.UPDATE_RETURN_ORDER);
        applicationEventPublisher.publishEvent(orderEvent);
        savedReturnOrder = this.save(savedReturnOrder);
        loyaltyInvoiceProducerService.createOrUpdateLoyaltyReceiptInvoice(savedReturnOrder);
        return savedReturnOrder;
    }

    @Override
    public ReturnOrderModel findByIdAndCompanyId(Long returnOrderId, Long companyId) {
        return returnOrderRepository.findByIdAndCompanyId(returnOrderId, companyId);
    }

    @Override
    public Page<ReturnOrderModel> findAllByCompanyId(Long companyId, Pageable pageable) {
        return returnOrderRepository.findAllByCompanyId(companyId, pageable);
    }

    @Override
    public ReturnOrderModel save(ReturnOrderModel returnOrderModel) {
        ReturnOrderModel model = returnOrderRepository.save(returnOrderModel);
        returnOrdersProducerService.sendReturnOrdersKafka(model);
        if (model.getExchangeOrder() != null) {
            OrderEvent event = new OrderEvent(model.getExchangeOrder());
            event.setCurrentUserId(model.getExchangeOrder().getCreateByUser());
            producerService.sendOrderKafka(event);
        }
        return model;
    }

    @Override
    public List<ReturnOrderModel> findAllByOriginOrder(OrderModel source) {
        return returnOrderRepository.findAllByOriginOrder(source);
    }

    @Override
    public OrderModel getOriginOrderOf(ReturnOrderModel returnOrderModel) {
        return orderRepository.findOriginOrderOf(returnOrderModel.getId());
    }

    @Override
    public Page<ReturnOrderModel> search(ReturnOrderSpecification specification, Pageable pageable) {
        return returnOrderRepository.findAll(specification, pageable);
    }

    @Override
    public ReturnRewardRedeemData getReturnRewardRedeem(ReturnOrderCommerceParameter parameter) {
        OrderModel originOrder = parameter.getOriginOrder();
        ReturnRewardRedeemData data = new ReturnRewardRedeemData();
        if (StringUtils.isNotEmpty(originOrder.getCardNumber())) {
            LoyaltyCardData loyaltyCardData = loyaltyService.findByCardNumber(originOrder.getCardNumber(), originOrder.getCompanyId());
            data.setAvailablePoint(loyaltyCardData.getPointAmount());
            data.setPendingPoint(loyaltyCardData.getPendingAmount());
            data.setNewAvailablePoint(data.getAvailablePoint());
        }
        if (!checkOriginOrderHasTransactionAndCardNumber(originOrder)) {
            return data;
        }
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCode(originOrder.getCode());
        double conversionRate = CommonUtils.readValue(loyaltyTransactionModel.getConversionRate());
        data.setConversionRate(conversionRate);

        double remainMoney = calculationService.calculateRemainCashAmount(parameter);
        if (originOrder.getTotalRewardAmount() != null && originOrder.getTotalRewardAmount() > 0) {
            double revertAmount = calculationService.calculateMaxRevertAmount(parameter.getReturnOrderRequest(), originOrder) / conversionRate;
            data.setRevertPoint(revertAmount);
        }
        if (originOrder.getRedeemAmount() != null && originOrder.getRedeemAmount() > 0) {
            double remainRedeem = (originOrder.getRedeemAmount() - CommonUtils.readValue(originOrder.getRefundAmount())) / conversionRate;
            data.setRemainRedeemPoint(remainRedeem);
            double refundPoint;
            if (remainMoney <= 0) {
                refundPoint = Math.ceil(calculationService.calculateMaxRefundAmount(parameter) / conversionRate);
            } else {
                refundPoint = Math.floor(calculationService.calculateMaxRefundAmount(parameter) / conversionRate);
            }
            data.setRefundPoint(refundPoint);
        }

        data.setRemainMoney(remainMoney);
        data.setRevertPoint(data.getRevertPoint() == null ? 0 : data.getRevertPoint());
        data.setRefundPoint(data.getRefundPoint() == null ? 0 : data.getRefundPoint());
        data.setAvailablePoint(CommonUtils.readValue(data.getAvailablePoint()));
        data.setPendingPoint(CommonUtils.readValue(data.getPendingPoint()));
        data.setRemainRedeemPoint(CommonUtils.readValue(data.getRemainRedeemPoint()));
        double availableRevertPoint = data.getAvailablePoint() + data.getPendingPoint() + data.getRefundPoint();
        double compensationPoint = 0;
        double advancePoints = 0;
        double newAvailablePoint = 0;
        double revertPoint = getRevertPointByOriginOrder(data, originOrder);
        if (revertPoint - availableRevertPoint > 0) {
            compensationPoint = revertPoint - availableRevertPoint;
            advancePoints = revertPoint - compensationPoint - data.getAvailablePoint();
            double availablePoint = data.getAvailablePoint();
            newAvailablePoint = data.getAvailablePoint() - availablePoint + data.getRefundPoint() - advancePoints;
        } else {
            newAvailablePoint = data.getAvailablePoint() - revertPoint + data.getRefundPoint() - advancePoints;
        }
        data.setNewAvailablePoint(newAvailablePoint);
        return data;
    }

    private double getRevertPointByOriginOrder(ReturnRewardRedeemData data, OrderModel originOrder) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(
                originOrder.getCode(), Arrays.asList(TransactionType.AWARD.name())
        );
        if (loyaltyTransactionModel == null) {
            return 0d;
        }

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setType(TransactionType.AWARD.name());
        transactionRequest.setCompanyId(originOrder.getCompanyId());
        transactionRequest.setInvoiceNumber(loyaltyTransactionModel.getInvoiceNumber());
        TransactionData transactionData = loyaltyService.findByInvoiceNumberAndCompanyIdAndType(transactionRequest);
        if (!OrderStatus.COMPLETED.code().equals(transactionData.getStatus())) {
            return 0d;
        }

        return data.getRevertPoint();
    }

    @Override
    public void updateReturnOrder(KafkaMessage<ReturnOrderBillDTO> returnOrderBillMessage) {
        ReturnOrderBillDTO returnOrderBillDto = returnOrderBillMessage.getContent();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(returnOrderBillDto.toString());
        }
        UpdateReturnOrderBillDTO updateReturnOrderBillDTO = updateReturnOrderEntriesConverter.convert(returnOrderBillDto);
        ReturnOrderModel returnOrderModel = returnOrderRepository.findByIdAndCompanyId(updateReturnOrderBillDTO.getReturnOrderId(),
                updateReturnOrderBillDTO.getCompanyId());
        if (returnOrderModel == null) {
            LOGGER.error("NOT FOUND ReturnOrderModel: {}, companyId: {}", updateReturnOrderBillDTO.getReturnOrderId(),
                    updateReturnOrderBillDTO.getCompanyId());
            return;
        }

        OrderModel originOrder = returnOrderModel.getOriginOrder();
        updateReturnOrderBillDTO.setOriginOrder(originOrder);
        double diffRevertAmount = orderService.updateAndCalculateDiffRevertAmountOfReturnEntries(updateReturnOrderBillDTO);
        double newRevertAmount = CommonUtils.readValue(returnOrderModel.getRevertAmount()) + diffRevertAmount;
        returnOrderModel.setRevertAmount(newRevertAmount);
        returnOrderRepository.updateRevertAmount(newRevertAmount, returnOrderModel.getId(), returnOrderModel.getCompanyId());
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(originOrder.getCode(),
                Arrays.asList(TransactionType.REVERT.name()));
        if (loyaltyTransactionModel != null) {
            loyaltyUpdateTransactionProducer.updateRevertTransactionKafka(originOrder, newRevertAmount);
            loyaltyInvoiceProducerService.createOrUpdateLoyaltyReceiptInvoice(returnOrderModel);
        }
    }

    @Override
    public ReturnOrderModel findByExportExternalIdAndCompanyId(Long exportExternalId, Long companyId) {
        return returnOrderRepository.findByExportExternalIdAndCompanyId(exportExternalId, companyId);
    }

    @Override
    @Transactional
    public ReturnOrderModel onlySave(ReturnOrderModel returnOrderModel) {
        return returnOrderRepository.save(returnOrderModel);
    }

    @Override
    public void linkReturnOrderForBill(Long companyId) {
        LOGGER.info("=======  start link return");
        Long start = System.currentTimeMillis();
        List<ReturnOrderModel> returnOrderModels = returnOrderRepository.findAllByCompanyIdAndExternalIdIsNotNull(companyId);
        if (CollectionUtils.isNotEmpty(returnOrderModels)) {
            for (ReturnOrderModel returnOrderModel : returnOrderModels) {
                try {
                    LinkReturnOrderforbillRequest request = new LinkReturnOrderforbillRequest();
                    request.setCompanyId(companyId);
                    request.setExportExternalId(returnOrderModel.getExportExternalId());
                    request.setReturnOrderId(returnOrderModel.getId());
                    OrderBillLinkDTO orderBillLinkDTOS = orderBillLinkDTOConverter.convert(returnOrderModel.getOriginOrder());
                    request.setOrderBillLinkDTO(orderBillLinkDTOS);
                    Long billId = billService.linkReturnOrderforbill(request);
                    returnOrderModel.setBillId(billId);
                    returnOrderRepository.save(returnOrderModel);
                } catch (RuntimeException e) {
                    LOGGER.error("=======  link return order for bill : fail: {}, error: {} ", returnOrderModel.getId(), e.getMessage());
                    LOGGER.error("error: {} ", e);
                }
            }
            LOGGER.info("=======  end link return order time: {}", System.currentTimeMillis() - start);
        }
    }

    private void populateRevertAndRefundAmount(ReturnOrderCommerceParameter commerceParameter, ReturnOrderModel returnOrderModel) {
        OrderModel originOrder = commerceParameter.getOriginOrder();
        if (!checkOriginOrderHasTransactionAndCardNumber(originOrder)) {
            return;
        }
        ReturnRewardRedeemData returnRewardRedeemData = getReturnRewardRedeem(commerceParameter);
        returnOrderModel.setConversionRate(returnRewardRedeemData.getConversionRate());
        double conversionRate = CommonUtils.readValue(returnRewardRedeemData.getConversionRate());
        if (originOrder.getRedeemAmount() != null) {
            double remainRedeem = originOrder.getRedeemAmount() - CommonUtils.readValue(originOrder.getRefundAmount());
            returnOrderModel.setRedeemAmount(remainRedeem);
        }
        ReturnOrderRequest request = commerceParameter.getReturnOrderRequest();
        double refundAmount = CommonUtils.readValue(request.getRefundAmount());
        double remainMoney = CommonUtils.readValue(returnRewardRedeemData.getRemainMoney());
        double validateRefundAmount = remainMoney + refundAmount
                - CommonUtils.readValue(commerceParameter.getBillRequest().getFinalCost());

        if (validateRefundAmount < 0 && remainMoney > 0) {
            ErrorCodes error = ErrorCodes.INVALID_REFUND_AMOUNT;
            throw new ServiceException(error.code(), error.message(), error.httpStatus());
        }
        if (refundAmount > 0) {
            double compareRefund = refundAmount -
                    conversionRate * CommonUtils.readValue(returnRewardRedeemData.getRefundPoint());
            if (compareRefund > 0) {
                ErrorCodes error = ErrorCodes.INVALID_REFUND_AMOUNT;
                throw new ServiceException(error.code(), error.message(), error.httpStatus());
            }
            TransactionData transactionData = loyaltyService.refund(originOrder, returnOrderModel, refundAmount);
            double totalRefundAmount = CommonUtils.readValue(transactionData.getRefundAmount()) + CommonUtils.readValue(originOrder.getRefundAmount());
            originOrder.setRefundAmount(totalRefundAmount);
            returnOrderModel.setRefundAmount(request.getRefundAmount());
        }

        double revertPoint = CommonUtils.readValue(returnRewardRedeemData.getRevertPoint());
        if (revertPoint > 0) {
            double revertAmount;
            double availableRevertPoint = CommonUtils.readValue(returnRewardRedeemData.getAvailablePoint()) +
                    CommonUtils.readValue(returnRewardRedeemData.getPendingPoint()) + CommonUtils.readValue(request.getRefundAmount()) / conversionRate;
            if (availableRevertPoint - revertPoint >= 0) {
                revertAmount = revertPoint * conversionRate;
            } else {
                revertAmount = availableRevertPoint * conversionRate;
                double compensationRevert = (revertPoint - availableRevertPoint) * conversionRate;
                returnOrderModel.setCompensateRevert(compensationRevert);
            }
            TransactionData transactionData = loyaltyService.revert(originOrder, returnOrderModel, revertAmount);
            returnOrderModel.setRevertAmount(transactionData.getRevertAmount());
        }

    }


    @Autowired
    public void setReturnOrderRepository(ReturnOrderRepository returnOrderRepository) {
        this.returnOrderRepository = returnOrderRepository;
    }

    private OrderModel updateOriginOrder(OrderModel originOrder, ReturnOrderRequest request) {
        Map<Integer, AbstractOrderEntryModel> orderEntryMap = originOrder.getEntries().stream()
                .collect(Collectors.toMap(AbstractOrderEntryModel::getEntryNumber, e -> e));
        for (ReturnOrderEntryRequest entryRequest : request.getReturnOrderEntries()) {
            AbstractOrderEntryModel entryModel = orderEntryMap.get(entryRequest.getEntryNumber());
            Long totalReturn = CommonUtils.readValue(entryModel.getReturnQuantity()) + CommonUtils.readValue(entryRequest.getQuantity());
            entryModel.setReturnQuantity(totalReturn);
        }
        return orderService.save(originOrder);
    }

    private boolean checkOriginOrderHasTransactionAndCardNumber(OrderModel originOrder) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCode(originOrder.getCode());
        if (loyaltyTransactionModel == null || originOrder.getCardNumber() == null) {
            return false;
        }
        return true;
    }

    private void populateBill(ReturnOrderCommerceParameter parameter, ReturnOrderModel returnOrderModel) {
        BillRequest billRequest = parameter.getBillRequest();
        billRequest.setReturnOrderId(returnOrderModel.getId());
        Long longBillId = billService.createBillForReturnOrder(billRequest);
        returnOrderModel.setBillId(longBillId);
    }

    @Override
    public List<ReturnOrderModel> findAllByCompanyIdAndCreatedTimeGreaterThanEqual(Long companyId, Date fromDate) {
        return returnOrderRepository.findAllByCompanyIdAndCreatedTimeGreaterThanEqual(companyId, fromDate);
    }

    @Override
    public List<ReturnOrderModel> findAllByCompanyId(Long companyId) {
        return returnOrderRepository.findAllByCompanyId(companyId);
    }

    @Override
    public double sumVatReturnOrderForOriginOrder(OrderModel originOrder) {
        double vat = 0;
        List<ReturnOrderModel> list = returnOrderRepository.findAllByOriginOrder(originOrder);
        if (CollectionUtils.isNotEmpty(list)) {
            vat = list.stream().filter(r -> r.getVat() != null).mapToDouble(ReturnOrderModel::getVat).sum();
        }
        return vat;
    }

    @Autowired
    public void setDefaultCommercePlaceOrderStrategy(DefaultCommercePlaceOrderStrategy defaultCommercePlaceOrderStrategy) {
        this.defaultCommercePlaceOrderStrategy = defaultCommercePlaceOrderStrategy;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Autowired
    public void setReturnOrdersProducerService(ReturnOrdersProducerService returnOrdersProducerService) {
        this.returnOrdersProducerService = returnOrdersProducerService;
    }

    @Autowired
    public void setProducerService(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setLoyaltyTransactionService(LoyaltyTransactionService loyaltyTransactionService) {
        this.loyaltyTransactionService = loyaltyTransactionService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    @Qualifier("updateReturnOrderEntriesConverter")
    public void setUpdateReturnOrderEntriesConverter(Converter<ReturnOrderBillDTO, UpdateReturnOrderBillDTO> updateReturnOrderEntriesConverter) {
        this.updateReturnOrderEntriesConverter = updateReturnOrderEntriesConverter;
    }

    @Autowired
    public void setLoyaltyUpdateTransactionProducer(LoyaltyTransactionProducerService loyaltyUpdateTransactionProducer) {
        this.loyaltyUpdateTransactionProducer = loyaltyUpdateTransactionProducer;
    }

    @Autowired
    public void setLoyaltyInvoiceProducerService(LoyaltyInvoiceProducerService loyaltyInvoiceProducerService) {
        this.loyaltyInvoiceProducerService = loyaltyInvoiceProducerService;
    }

    @Autowired
    public void setOrderBillLinkDTOConverter(Converter<OrderModel, OrderBillLinkDTO> orderBillLinkDTOConverter) {
        this.orderBillLinkDTOConverter = orderBillLinkDTOConverter;
    }
}
