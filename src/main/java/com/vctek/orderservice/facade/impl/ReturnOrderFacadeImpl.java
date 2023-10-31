package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.dto.request.ReturnOrderUpdateParameter;
import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.event.ReturnOrderEventType;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.ReturnOrderFacade;
import com.vctek.orderservice.feignclient.dto.UpdateReturnOrderBillRequest;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.kafka.producer.ReturnOrdersProducerService;
import com.vctek.orderservice.kafka.producer.UpdateReturnOrderProducer;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.service.specification.ReturnOrderSpecification;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.cluster.lock.support.DelegatingDistributedLock;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReturnOrderFacadeImpl implements ReturnOrderFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnOrderFacadeImpl.class);
    private ReturnOrderService returnOrderService;
    private Converter<ReturnOrderRequest, ReturnOrderCommerceParameter> returnOrderCommerceParameterConverter;
    private Converter<ReturnOrderModel, ReturnOrderData> basicReturnOrderConverter;
    private ApplicationEventPublisher applicationEventPublisher;
    private Populator<ReturnOrderModel, ReturnOrderData> returnOrderDetailPopulator;
    private GenerateCartCodeService generateCartCodeService;
    private OrderService orderService;
    private Converter<OrderModel, OrderData> orderConverter;
    private CommerceCartService commerceCartService;
    private AuthService authService;
    private UpdateReturnOrderProducer updateReturnOrderProducer;
    private BillService billService;
    private LoyaltyService loyaltyService;
    private InvoiceService invoiceService;
    private Populator<List<PaymentTransactionRequest>, OrderModel> orderPaymentTransactionRequestPopulator;
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;
    private RedisLockService redisLockService;
    private ReturnOrdersProducerService returnOrdersProducerService;

    @Override
    public ReturnOrderData create(ReturnOrderRequest returnOrderRequest) {
        String lockKey = "CREATE_RETURN_ORDER:" + returnOrderRequest.getCompanyId() + ":" + returnOrderRequest.getOriginOrderCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            ReturnOrderCommerceParameter parameter = returnOrderCommerceParameterConverter.convert(returnOrderRequest);
            ReturnOrderModel returnOrderModel = returnOrderService.create(parameter);

            ReturnOrderEvent event = new ReturnOrderEvent(returnOrderModel, ReturnOrderEventType.CREATE);
            event.setBillRequest(parameter.getBillRequest());
            applicationEventPublisher.publishEvent(event);
            return basicReturnOrderConverter.convert(returnOrderModel);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ReturnOrderData getDetail(Long returnOrderId, Long companyId) {
        ReturnOrderModel returnOrderModel = returnOrderService.findByIdAndCompanyId(returnOrderId, companyId);
        if (returnOrderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        ReturnOrderData returnOrderData = basicReturnOrderConverter.convert(returnOrderModel);
        returnOrderDetailPopulator.populate(returnOrderModel, returnOrderData);
        return returnOrderData;
    }

    @Override
    @Transactional
    public OrderData createOrGetExchangeOrder(ReturnOrderUpdateParameter parameter) {
        ReturnOrderModel returnOrderModel = returnOrderService.findByIdAndCompanyId(parameter.getReturnOrderId(),
                parameter.getCompanyId());
        if (returnOrderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (returnOrderModel.getExchangeOrder() != null) {
            return orderConverter.convert(returnOrderModel.getExchangeOrder());
        }

        OrderModel originOrder = returnOrderModel.getOriginOrder();
        if (originOrder == null) {
            ErrorCodes err = ErrorCodes.RETURN_ORDER_HAS_NOT_ORIGIN_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderModel exchangeOrder = new OrderModel();
        exchangeOrder.setExchange(true);
        exchangeOrder.setType(originOrder.getType());
        exchangeOrder.setCompanyId(originOrder.getCompanyId());
        exchangeOrder.setWarehouseId(originOrder.getWarehouseId());
        exchangeOrder.setCustomerId(originOrder.getCustomerId());
        exchangeOrder.setReturnOrder(returnOrderModel);
        exchangeOrder.setCurrencyCode(originOrder.getCurrencyCode());
        exchangeOrder.setCreateByUser(authService.getCurrentUserId());
        exchangeOrder.setOrderStatus(OrderStatus.COMPLETED.code());
        exchangeOrder.setCardNumber(parameter.getExchangeLoyaltyCard());
        exchangeOrder.setPriceType(parameter.getPriceType());
        exchangeOrder.setEmployeeId(originOrder.getEmployeeId());
        exchangeOrder.setAge(originOrder.getAge());
        exchangeOrder.setGender(originOrder.getGender());
        exchangeOrder.setOrderSourceModel(originOrder.getOrderSourceModel());
        OrderModel savedModel = orderService.save(exchangeOrder);
        savedModel.setCode(generateCartCodeService.generateCartCode(savedModel));
        returnOrderModel.setExchangeOrder(exchangeOrder);
        returnOrderModel.setExchangeOrderCode(savedModel.getCode());
        orderService.save(savedModel);
        applicationEventPublisher.publishEvent(new OrderEvent(savedModel));
        return orderConverter.convert(savedModel);
    }

    @Override
    @Transactional
    public OrderData doChangeWarehouse(ReturnOrderUpdateParameter parameter) {
        ReturnOrderModel returnOrder = returnOrderService.findByIdAndCompanyId(parameter.getReturnOrderId(),
                parameter.getCompanyId());
        OrderModel exchangeOrder = returnOrder.getExchangeOrder();
        if (exchangeOrder == null) {
            ErrorCodes err = ErrorCodes.INVALID_EXCHANGE_ORDER_CODE_IN_RETURN_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (exchangeOrder != null && CollectionUtils.isNotEmpty(exchangeOrder.getEntries())) {
            ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_TO_OTHER_WAREHOUSE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (exchangeOrder.getWarehouseId() == null ||
                !exchangeOrder.getWarehouseId().equals(parameter.getWarehouseId())) {
            exchangeOrder.setWarehouseId(parameter.getWarehouseId());
            OrderModel savedModel = orderService.save(exchangeOrder);
            applicationEventPublisher.publishEvent(new OrderEvent(savedModel));
            //TODO support change all items to other warehouse? if yes, must revert all stock of old warehouses
            if (CollectionUtils.isNotEmpty(exchangeOrder.getEntries())) {
                CommerceAbstractOrderParameter commerceAbtractOrderParameter = new CommerceAbstractOrderParameter();
                commerceAbtractOrderParameter.setOrder(exchangeOrder);
                commerceCartService.removeAllEntries(commerceAbtractOrderParameter);
            }
        }

        return orderConverter.convert(exchangeOrder);
    }

    @Override
    @Transactional
    public ReturnOrderData updateInfo(ReturnOrderRequest returnOrderRequest) {
        String lockKey = "UPDATE_INFO_RETURN_ORDER:" + returnOrderRequest.getCompanyId() + ":" + returnOrderRequest.getId();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            ReturnOrderModel returnOrderModel = returnOrderService.findByIdAndCompanyId(returnOrderRequest.getId(),
                    returnOrderRequest.getCompanyId());
            validateRefundPoint(returnOrderModel, returnOrderRequest);
            returnOrderModel.setNote(returnOrderRequest.getNote());
            returnOrderModel.setShippingFee(returnOrderRequest.getShippingFee());
            returnOrderModel.setCompanyShippingFee(returnOrderRequest.getCompanyShippingFee());
            returnOrderModel.setCollaboratorShippingFee(returnOrderRequest.getCollaboratorShippingFee());
            returnOrderModel.setVat(returnOrderRequest.getVat());
            if (returnOrderRequest.getRefundAmount() != null
                    && !returnOrderRequest.getRefundAmount().equals(CommonUtils.readValue(returnOrderModel.getRefundAmount()))) {
                updateRefund(returnOrderModel, returnOrderRequest);
                loyaltyService.updateRefund(returnOrderModel);
                invoiceService.updateRefundInvoice(returnOrderModel);
            }

            updateExchangeOrder(returnOrderModel, returnOrderRequest);
            returnOrderService.save(returnOrderModel);

            updateVatNumberInOrder(returnOrderRequest, returnOrderModel);
            ReturnOrderEvent event = new ReturnOrderEvent(returnOrderModel, ReturnOrderEventType.UPDATE);
            applicationEventPublisher.publishEvent(event);
            ReturnOrderData returnOrderData = basicReturnOrderConverter.convert(returnOrderModel);
            returnOrderDetailPopulator.populate(returnOrderModel, returnOrderData);
            return returnOrderData;
        } finally {
            lock.unlock();
        }
    }

    private void updateExchangeOrder(ReturnOrderModel returnOrderModel, ReturnOrderRequest returnOrderRequest) {
        if (returnOrderModel.getExchangeOrder() != null) {
            if (CollectionUtils.isNotEmpty(returnOrderRequest.getExchangePayments())) {
                OrderModel exchangeModel = returnOrderModel.getExchangeOrder();
                orderPaymentTransactionRequestPopulator.populate(returnOrderRequest.getExchangePayments(), exchangeModel);
                TransactionData redeemTransactionData = loyaltyService.updateRedeem(exchangeModel);
                invoiceService.saveInvoices(exchangeModel, exchangeModel.getCustomerId());
                exchangeModel.setRedeemAmount(redeemTransactionData.getRedeemAmount());
                orderService.save(exchangeModel);
            }
            loyaltyInvoiceProducerService.createOrUpdateLoyaltyImbursementInvoice(returnOrderModel.getExchangeOrder());
        }
    }

    private void validateRefundPoint(ReturnOrderModel returnOrderModel, ReturnOrderRequest returnOrderRequest) {
        OrderModel orderModel = returnOrderModel.getOriginOrder();
        if (returnOrderModel.getRefundAmount() != null && !(returnOrderModel.getRefundAmount().equals(returnOrderRequest.getRefundAmount()))) {
            List<AbstractOrderEntryModel> entryModel = orderModel.getEntries().stream().filter(entry -> !(entry.getQuantity().equals(entry.getReturnQuantity()))).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(entryModel)) {
                ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_REFUND_POINTS;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    private void updateRefund(ReturnOrderModel returnOrderModel, ReturnOrderRequest returnOrderRequest) {
        double oldRefundAmountInReturn = CommonUtils.readValue(returnOrderModel.getRefundAmount());
        double diffAmount = returnOrderRequest.getRefundAmount() - oldRefundAmountInReturn;
        OrderModel originOrder = returnOrderModel.getOriginOrder();
        double oldRefundAmountInOrder = CommonUtils.readValue(originOrder.getRefundAmount());
        double newRefundAmountInOrder = oldRefundAmountInOrder + diffAmount;
        originOrder.setRefundAmount(newRefundAmountInOrder);
        returnOrderModel.setRefundAmount(returnOrderRequest.getRefundAmount());

    }

    protected void updateVatNumberInOrder(ReturnOrderRequest returnOrderRequest, ReturnOrderModel returnOrderModel) {
        if (StringUtils.isNotEmpty(returnOrderRequest.getVatNumber())) {
            OrderModel orderModel = returnOrderModel.getExchangeOrder();
            orderModel.setVatNumber(returnOrderRequest.getVatNumber());
            orderService.save(orderModel);
        }
    }

    @Override
    public void updateReport(ReturnOrderSearchRequest request) {
        ReturnOrderSpecification specification = new ReturnOrderSpecification(request);
        Pageable pageable = PageRequest.of(0, 100);
        while (true) {
            Page<ReturnOrderModel> searchData = returnOrderService.search(specification, pageable);
            List<ReturnOrderModel> returnOrderModels = searchData.getContent();
            if (CollectionUtils.isEmpty(returnOrderModels)) {
                break;
            }

            for (ReturnOrderModel returnOrderModel : returnOrderModels) {
                updateReturnOrderProducer.process(returnOrderModel);
            }

            LOGGER.info("Finish update return order report: {} - page: {}/{} items", returnOrderModels.size(),
                    pageable.getPageNumber(),
                    searchData.getTotalElements());
            pageable = pageable.next();
        }
    }

    @Override
    public void updateOriginOrderBill(Long companyId) {
        ReturnOrderSearchRequest request = new ReturnOrderSearchRequest();
        request.setCompanyId(companyId);
        ReturnOrderSpecification specification = new ReturnOrderSpecification(request);
        Pageable pageable = PageRequest.of(0, 100);
        while (true) {
            Page<ReturnOrderModel> searchData = returnOrderService.search(specification, pageable);
            List<ReturnOrderModel> returnOrderModels = searchData.getContent();
            if (CollectionUtils.isEmpty(returnOrderModels)) {
                break;
            }
            UpdateReturnOrderBillRequest returnOrderBillRequest;
            for (ReturnOrderModel returnOrderModel : returnOrderModels) {
                OrderModel originOrder = returnOrderModel.getOriginOrder();
                returnOrderBillRequest = new UpdateReturnOrderBillRequest();
                returnOrderBillRequest.setBillId(returnOrderModel.getBillId());
                returnOrderBillRequest.setCompanyId(returnOrderModel.getCompanyId());
                returnOrderBillRequest.setOriginOrderCode(originOrder.getCode());
                returnOrderBillRequest.setReturnOrderId(returnOrderModel.getId());
                billService.updateOriginOrderCode(returnOrderBillRequest);
            }

            LOGGER.info("Finish update return order report: {} - page: {}/{} items", returnOrderModels.size(),
                    pageable.getPageNumber(),
                    searchData.getTotalElements());
            pageable = pageable.next();
        }
    }

    @Override
    public ReturnRewardRedeemData getReturnRewardRedeem(ReturnOrderRequest request) {
        ReturnOrderCommerceParameter parameter = returnOrderCommerceParameterConverter.convert(request);
        return returnOrderService.getReturnRewardRedeem(parameter);
    }

    @Override
    public void createRevenueReturnOrder(ReturnOrderSearchRequest request) {
        if (request.getId() != null) {
            ReturnOrderModel model = returnOrderService.findByIdAndCompanyId(request.getId(), request.getCompanyId());
            if (model == null) {
                ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            returnOrdersProducerService.sendReturnOrdersKafka(model);
        }

        List<ReturnOrderModel> modelList = new ArrayList<>();
        if (request.getFromCreatedTime() != null) {
            modelList = returnOrderService.findAllByCompanyIdAndCreatedTimeGreaterThanEqual(
                    request.getCompanyId(), request.getFromCreatedTime());
        } else if (request.getCompanyId() != null) {
            modelList = returnOrderService.findAllByCompanyId(request.getCompanyId());
        }

        if (CollectionUtils.isNotEmpty(modelList)) {
            for (ReturnOrderModel model : modelList) {
                returnOrdersProducerService.sendReturnOrdersKafka(model);
            }
        }
    }

    @Override
    public ReturnOrderVatData getInfoVatOfReturnOrderWithOriginOrderCode(String originOrderCode, Long companyId) {
        OrderModel originOrder = orderService.findByCodeAndCompanyIdAndDeleted(originOrderCode, companyId, false);
        if (originOrder == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        ReturnOrderVatData returnOrderVatData = new ReturnOrderVatData();

        double originOrderVat = CommonUtils.readValue(originOrder.getVat());
        if (DiscountType.PERCENT.toString().equals(originOrder.getVatType())) {
            originOrderVat = originOrder.getTotalPrice() * originOrder.getVat() / 100;
        }

        returnOrderVatData.setOriginOrderVat(originOrderVat);
        double vat = returnOrderService.sumVatReturnOrderForOriginOrder(originOrder);
        returnOrderVatData.setReturnOrderVat(originOrderVat - vat);
        return returnOrderVatData;
    }

    @Override
    @Transactional
    public ReturnOrderData updateRefundPoint(ReturnOrderRequest returnOrderRequest) {
        ReturnOrderModel returnOrderModel = returnOrderService.findByIdAndCompanyId(returnOrderRequest.getId(),
                returnOrderRequest.getCompanyId());
        if (returnOrderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (returnOrderRequest.getRefundAmount() != null) {
            updateRefund(returnOrderModel, returnOrderRequest);
            loyaltyService.updateRefund(returnOrderModel);
            invoiceService.updateRefundInvoice(returnOrderModel);
        }

        returnOrderService.save(returnOrderModel);

        ReturnOrderEvent event = new ReturnOrderEvent(returnOrderModel, ReturnOrderEventType.UPDATE);
        applicationEventPublisher.publishEvent(event);
        ReturnOrderData returnOrderData = basicReturnOrderConverter.convert(returnOrderModel);
        returnOrderDetailPopulator.populate(returnOrderModel, returnOrderData);
        return returnOrderData;
    }

    @Autowired
    public void setReturnOrderCommerceParameterConverter(Converter<ReturnOrderRequest, ReturnOrderCommerceParameter> returnOrderCommerceParameterConverter) {
        this.returnOrderCommerceParameterConverter = returnOrderCommerceParameterConverter;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    public void setBasicReturnOrderConverter(Converter<ReturnOrderModel, ReturnOrderData> basicReturnOrderConverter) {
        this.basicReturnOrderConverter = basicReturnOrderConverter;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    @Qualifier("returnOrderDetailPopulator")
    public void setReturnOrderDetailPopulator(Populator<ReturnOrderModel, ReturnOrderData> returnOrderDetailPopulator) {
        this.returnOrderDetailPopulator = returnOrderDetailPopulator;
    }

    @Autowired
    public void setGenerateCartCodeService(GenerateCartCodeService generateCartCodeService) {
        this.generateCartCodeService = generateCartCodeService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setOrderConverter(Converter<OrderModel, OrderData> orderConverter) {
        this.orderConverter = orderConverter;
    }

    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    public void setUpdateReturnOrderProducer(UpdateReturnOrderProducer updateReturnOrderProducer) {
        this.updateReturnOrderProducer = updateReturnOrderProducer;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Autowired
    @Qualifier("orderPaymentTransactionRequestPopulator")
    public void setOrderPaymentTransactionRequestPopulator(Populator<List<PaymentTransactionRequest>, OrderModel> orderPaymentTransactionRequestPopulator) {
        this.orderPaymentTransactionRequestPopulator = orderPaymentTransactionRequestPopulator;
    }

    @Autowired
    public void setLoyaltyInvoiceProducerService(LoyaltyInvoiceProducerService loyaltyInvoiceProducerService) {
        this.loyaltyInvoiceProducerService = loyaltyInvoiceProducerService;
    }

    @Autowired
    public void setRedisLockService(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }

    @Autowired
    public void setReturnOrdersProducerService(ReturnOrdersProducerService returnOrdersProducerService) {
        this.returnOrdersProducerService = returnOrdersProducerService;
    }
}
