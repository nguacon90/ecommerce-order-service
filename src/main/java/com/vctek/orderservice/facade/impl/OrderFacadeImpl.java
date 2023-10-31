package com.vctek.orderservice.facade.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.event.ReturnOrderEventType;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderFacade;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryDetailData;
import com.vctek.orderservice.kafka.producer.*;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.service.event.OrderHistoryEvent;
import com.vctek.orderservice.strategy.CommerceChangeOrderStatusStrategy;
import com.vctek.orderservice.strategy.CommercePlaceOrderStrategy;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.sync.Mutex;
import com.vctek.util.*;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class OrderFacadeImpl extends AbstractOrderFacade implements OrderFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderFacadeImpl.class);
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    private Converter<OrderRequest, CommerceCheckoutParameter> commerceCheckoutParameterConverter;
    private Converter<OrderRequest, UpdateOrderParameter> updateOrderParameterConverter;
    private Converter<OrderModel, OrderData> orderConverter;
    private Converter<OrderEntryDTO, CommerceAbstractOrderParameter> commerceOrderParameterConverter;
    private OrderService orderService;
    private CommerceCartService commerceCartService;
    private ApplicationEventPublisher applicationEventPublisher;
    private OrderHistoryService orderHistoryService;
    private OrderProducerService producerService;
    private CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy;
    private PermissionFacade permissionFacade;
    private RedisLockService redisLockService;
    private CouponService couponService;
    private Converter<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> commerceSubOrderEntryParameterConverter;
    private SubOrderEntryRepository subOrderEntryService;
    private ReturnOrdersProducerService returnOrdersProducerService;
    private Populator<ToppingItemRequest, ToppingItemParameter> toppingItemOrderParameterPopulator;
    private InvoiceService invoiceService;
    private OrderElasticSearchService orderElasticSearchService;
    private OrderBillLinkProducer orderBillLinkProducer;
    private CalculatePaidAmountOrderProducer calculatePaidAmountOrderProducer;
    private LoyaltyService loyaltyService;
    private UpdateOrderSequenceCacheService updateOrderSequenceCacheService;
    private InventoryService inventoryService;
    private ReturnOrderService returnOrderService;
    private UpdateReturnOrderProducer updateReturnOrderProducer;
    private AuthService authService;
    private ObjectMapper objectMapper;
    private Validator<CommerceAbstractOrderParameter> saleOffOrderEntryValidator;
    private Validator<CommerceAbstractOrderParameter> saleOffUpdateQuantityOrderEntryValidator;
    private BillService billService;

    @Override
    public OrderData placeOrder(OrderRequest orderRequest) {
        String lockKey = "PLACE_ORDER:" + orderRequest.getCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            CommerceCheckoutParameter checkoutParameter = commerceCheckoutParameterConverter.convert(orderRequest);
            CommerceOrderResult commerceOrderResult = commercePlaceOrderStrategy.placeOrder(checkoutParameter);
            OrderModel orderModel = commerceOrderResult.getOrderModel();
            LOGGER.info("PLACE ORDER SUCCESS: {'orderCode': {}, 'companyId': {}, 'finalPrice': {}, 'customerId': {}, 'createdBy': {}}",
                    orderModel.getCode(), orderModel.getCompanyId(), orderModel.getFinalPrice(), orderModel.getCustomerId(),
                    orderModel.getCreateByUser());
            return orderConverter.convert(orderModel);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderData findBy(String orderCode, Long companyId, String orderType, boolean isExchange) {
        OrderModel order;
        if (isExchange) {
            order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        } else {
            order = orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(orderCode, companyId, orderType, false);
        }
        validateAbstractOrder(order);
        return orderConverter.convert(order);
    }

    @Override
    public OrderData addEntryToOrder(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CommerceAbstractOrderParameter orderParameter = commerceOrderParameterConverter.convert(orderEntryDTO);
            CommerceCartModification commerceCartModification = commerceCartService.addEntryToOrder(orderParameter);

            OrderModel order = (OrderModel) commerceCartModification.getOrder();
            applicationEventPublisher.publishEvent(new OrderEvent(order));
            sendReturnOrdersKafkaAndEvent(order);
            return orderConverter.convert(order);
        }
    }

    protected void sendReturnOrdersKafkaAndEvent(OrderModel order) {
        if (order.getReturnOrder() != null) {
            returnOrdersProducerService.sendReturnOrdersKafka(order.getReturnOrder());
            if (order.isExchange()) {
                OrderEvent event = new OrderEvent(order);
                producerService.sendOrderKafka(event);
            }
            ReturnOrderEvent event = new ReturnOrderEvent(order.getReturnOrder(),
                    ReturnOrderEventType.UPDATE_EXCHANGE_ORDER);
            applicationEventPublisher.publishEvent(event);
        }
    }

    @Override
    public OrderData updateEntry(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            DelegatingDistributedLock lock = null;
            try {
                OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderEntryDTO.getOrderCode(), orderEntryDTO.getCompanyId(), false);
                validateAbstractOrder(order);
                if (OrderType.ONLINE.toString().equals(order.getType())) {
                    String lockKey = "changeOrderStatus:" + orderEntryDTO.getOrderCode();
                    lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
                    if (lock.tryLock()) {
                        LOGGER.debug("LOCKED ONLINE CHANGE STATUS");
                    }
                }
                if (!updateOrderSequenceCacheService.isValidTimeRequest("updateEntryOrder", orderEntryDTO.getOrderCode(),
                        orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                    return orderConverter.convert(order);
                }
                CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
                parameter.setOrder(order);
                parameter.setQuantity(orderEntryDTO.getQuantity());
                parameter.setEntryId(orderEntryDTO.getEntryId());
                saleOffUpdateQuantityOrderEntryValidator.validate(parameter);
                commerceCartService.updateOrderEntry(parameter);
                sendReturnOrdersKafkaAndEvent(order);
                return orderConverter.convert(order);
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    public OrderData updateDiscountOfEntry(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderEntryDTO.getOrderCode(), orderEntryDTO.getCompanyId(), false);
            validateAbstractOrder(order);
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updateDiscountOfEntryOrder", orderEntryDTO.getOrderCode(),
                    orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                return orderConverter.convert(order);
            }
            LOGGER.info("Update discount entry: code: {}, discount: {}", orderEntryDTO.getOrderCode(), orderEntryDTO.getDiscount());
            CurrencyType currencyType = CurrencyType.findByCode(orderEntryDTO.getDiscountType());
            if (currencyType == null) {
                ErrorCodes err = ErrorCodes.INVALID_DISCOUNT_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            validateDiscount(order, orderEntryDTO);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            parameter.setDiscount(orderEntryDTO.getDiscount());
            parameter.setDiscountType(orderEntryDTO.getDiscountType());
            parameter.setEntryId(orderEntryDTO.getEntryId());
            commerceCartService.updateDiscountForOrderEntry(parameter);

            sendReturnOrdersKafkaAndEvent(order);

            return orderConverter.convert(order);
        }
    }

    private void validateDiscount(OrderModel order, OrderEntryDTO orderEntryDTO) {
        if (OrderType.ONLINE.toString().equals(order.getType()) && !OrderStatus.COMPLETED.toString().equals(order.getOrderStatus())) {
            return;
        }

        Optional<AbstractOrderEntryModel> abstractOrderEntryModel = order.getEntries().stream().filter(i -> i.getId().equals(orderEntryDTO.getEntryId()) && !i.isGiveAway()).findFirst();
        boolean permissionConfirm = permissionFacade.checkPermission(PermissionCodes.CONFIRM_MAXIMUM_DISCOUNT_ORDER.code(),
                authService.getCurrentUserId(), order.getCompanyId());
        if (abstractOrderEntryModel.isPresent() && !permissionConfirm) {
            Map<Long, OrderSettingDiscountData> productSettingDiscount = commerceCartService.checkDiscountMaximumProduct(
                    order, orderEntryDTO.getProductId());
            OrderSettingDiscountData data = productSettingDiscount.get(orderEntryDTO.getProductId());
            AbstractOrderEntryModel entryModel = abstractOrderEntryModel.get();
            if (data == null) {
                return;
            }

            double maximumDiscount = data.getDiscount();
            double newDiscount = orderEntryDTO.getDiscount() / entryModel.getQuantity();
            DecimalFormat df = new DecimalFormat("#.#");
            CurrencyType currencyType = CurrencyType.findByCode(data.getDiscountType());
            String message = df.format(Math.round(data.getDiscount() * entryModel.getQuantity() * 10.0) / 10.0) + currencyType.code();
            if (com.vctek.util.CurrencyType.PERCENT.toString().equals(data.getDiscountType())) {
                maximumDiscount = CurrencyUtils.computeValue(data.getDiscount(), data.getDiscountType(), entryModel.getOriginBasePrice());
                message = df.format(Math.round(data.getDiscount() * 10.0) / 10.0) + currencyType.code();
                message += " (" + df.format(Math.round(maximumDiscount * entryModel.getQuantity() * 10.0) / 10.0) + "Đ)";
            }
            if (com.vctek.util.CurrencyType.PERCENT.toString().equals(orderEntryDTO.getDiscountType())) {
                newDiscount = CurrencyUtils.computeValue(orderEntryDTO.getDiscount(),
                        orderEntryDTO.getDiscountType(), entryModel.getTotalPrice() / entryModel.getQuantity());
            }
            if (maximumDiscount < newDiscount && order.getConfirmDiscountBy() == null) {
                ErrorCodes err = ErrorCodes.DISCOUNT_MUST_BE_LESS_SETTING_MAXIMUM_DISCOUNT;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{message});
            }
        }
    }

    @Override
    public OrderData updateDiscountOfOrder(CartDiscountRequest cartDiscountRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(cartDiscountRequest.getCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyId(cartDiscountRequest.getCode(),
                    cartDiscountRequest.getCompanyId());
            validateAbstractOrder(order);
            CurrencyType currencyType = CurrencyType.findByCode(cartDiscountRequest.getDiscountType());
            if (currencyType == null) {
                ErrorCodes err = ErrorCodes.INVALID_DISCOUNT_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updateDiscountOfOrder", cartDiscountRequest.getCode(),
                    null, cartDiscountRequest.getTimeRequest())) {
                return orderConverter.convert(order);
            }
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            parameter.setDiscount(cartDiscountRequest.getDiscount());
            parameter.setDiscountType(cartDiscountRequest.getDiscountType());
            commerceCartService.updateDiscountForOrder(parameter);

            sendReturnOrdersKafkaAndEvent(order);

            return orderConverter.convert(order);
        }
    }

    @Override
    public OrderData updateVatOfOrder(VatRequest vatRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(vatRequest.getCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyId(vatRequest.getCode(), vatRequest.getCompanyId());
            validateAbstractOrder(order);

            CurrencyType currencyType = CurrencyType.findByCode(vatRequest.getVatType());
            if (currencyType == null) {
                ErrorCodes err = ErrorCodes.INVALID_VAT_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updateVatOfOrder", vatRequest.getCode(),
                    null, vatRequest.getTimeRequest())) {
                return orderConverter.convert(order);
            }
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            parameter.setVat(vatRequest.getVat());
            parameter.setVatType(vatRequest.getVatType());
            commerceCartService.updateVatForCart(parameter);
            sendReturnOrdersKafkaAndEvent(order);
            return orderConverter.convert(order);
        }
    }

    @Override
    public OrderData updateOrderInfo(OrderRequest orderRequest) {
        String lockKey = "updateOrder:" + orderRequest.getCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            UpdateOrderParameter updateOrderParameter = updateOrderParameterConverter.convert(orderRequest);
            CommerceOrderResult commerceOrderResult = commercePlaceOrderStrategy.updateOrder(updateOrderParameter);
            OrderModel orderModel = commerceOrderResult.getOrderModel();
            return orderConverter.convert(orderModel);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderData updateInfoOnlineOrder(OrderRequest orderRequest) {
        String lockKey = "updateInfoOnlineOrder:" + orderRequest.getCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            UpdateOrderParameter updateOrderParameter = updateOrderParameterConverter.convert(orderRequest);
            CommerceOrderResult commerceOrderResult = commercePlaceOrderStrategy.updateCustomerInfoInOnlineOrder(updateOrderParameter);
            OrderModel orderModel = commerceOrderResult.getOrderModel();
            return orderConverter.convert(orderModel);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderData updatePriceOrderEntry(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyId(orderEntryDTO.getOrderCode(), orderEntryDTO.getCompanyId());
            validateAbstractOrder(order);
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updatePriceOfEntryOrder", orderEntryDTO.getOrderCode(),
                    orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                return orderConverter.convert(order);
            }

            validatePriceOrderEntry(order, orderEntryDTO);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            parameter.setEntryId(orderEntryDTO.getEntryId());
            parameter.setBasePrice(orderEntryDTO.getPrice());

            commerceCartService.updatePriceForCartEntry(parameter);

            sendReturnOrdersKafkaAndEvent(order);
            return orderConverter.convert(order);
        }
    }

    @Override
    public void changeStatusOrder(ChangeOrderStatusRequest request) {

        String lockKey = "changeOrderStatus:" + request.getOrderCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        try {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(request.getOrderCode(), request.getCompanyId(), false);
            validateAbstractOrder(order);
            validateComboQuantityWhenChangeStatusOrder(request, order);
            OrderStatus newStatus = OrderStatus.findByCode(request.getOrderStatus());
            OrderStatus oldStatus = OrderStatus.findByCode(order.getOrderStatus());

            if (newStatus == null || oldStatus == null || newStatus.equals(oldStatus)) {
                ErrorCodes err = ErrorCodes.INVALID_ORDER_STATUS_CHANGE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            if (SellSignal.ECOMMERCE_WEB.name().equals(order.getSellSignal()) && OrderStatus.CHANGE_TO_RETAIL.toString().equals(request.getOrderStatus())) {
                ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_TO_RETAIL_STATUS_ECOMMERCE_WEB;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            if (OrderStatus.PRE_ORDER.equals(newStatus)) {
                validateOrderContainComboEntryOrFnB(order);
            }

            CommerceChangeOrderStatusParameter parameter = new CommerceChangeOrderStatusParameter(order, oldStatus, newStatus);
            parameter.setCancelText(request.getCancelText());
            parameter.setConfirmDiscount(request.isConfirmDiscount());

            if (mustCheckPermissionOf(oldStatus)) {
                boolean hasPermission = checkPermissionToChangeCompletedOrder(order);
                if (!hasPermission) {
                    ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_COMPLETED_STATUS;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus());
                }
            }
            try {
                commerceChangeOrderStatusStrategy.changeStatusOrder(parameter);
            } catch (RuntimeException e) {
                billService.revertOnlineBillWhenError(oldStatus, newStatus, order);
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean checkPermissionToChangeCompletedOrder(OrderModel order) {
        boolean changeCompletedOrderStatus = permissionFacade.hasPermission(PermissionCodes.CHANGE_ORDER_STATUS_COMPLETED.code(),
                order.getCompanyId());
        if(changeCompletedOrderStatus) {
            return true;
        }

        return permissionFacade.hasPermission(PermissionCodes.EDIT_COMPLETED_ONLINE_ORDER.code(),
                order.getCompanyId());
    }

    private boolean mustCheckPermissionOf(OrderStatus oldStatus) {
        return OrderStatus.COMPLETED.equals(oldStatus) ||
                OrderStatus.ORDER_RETURN.equals(oldStatus) ||
                OrderStatus.CUSTOMER_CANCEL.equals(oldStatus) ||
                OrderStatus.SYSTEM_CANCEL.equals(oldStatus);
    }

    private void validateComboQuantityWhenChangeStatusOrder(ChangeOrderStatusRequest request, OrderModel order) {
        if (OrderStatus.CONFIRMED.code().equals(request.getOrderStatus())) {
            ComboData comboData;
            long totalItemQuantity;
            int subEntryTotalQuantity;
            for (AbstractOrderEntryModel entryModel : order.getEntries()) {
                if (StringUtils.isNotEmpty(entryModel.getComboType())) {
                    comboData = productService.getCombo(entryModel.getProductId(), order.getCompanyId());
                    if (comboData == null) {
                        ErrorCodes err = ErrorCodes.INVALID_COMBO_ID;
                        throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{entryModel.getProductId()});
                    }
                    totalItemQuantity = comboData.getTotalItemQuantity() * entryModel.getQuantity();
                    subEntryTotalQuantity = entryModel.getSubOrderEntries().stream()
                            .filter(se -> se.getQuantity() != null)
                            .mapToInt(SubOrderEntryModel::getQuantity).sum();
                    if (totalItemQuantity != subEntryTotalQuantity) {
                        ErrorCodes err = ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY;
                        throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{comboData.getName()});
                    }
                }
            }
        }

    }

    private void validateOrderContainComboEntryOrFnB(OrderModel order) {
        for (AbstractOrderEntryModel entry : order.getEntries()) {
            if (productService.isFnB(entry.getProductId())) {
                ErrorCodes err = ErrorCodes.CANNOT_PRE_ORDER_CONTAIN_FOOD_BEVERAGE_ENTRY;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }


    @Override
    @Transactional
    public String changeOrderToRetail(String orderCode, Long companyId) {
        OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateAbstractOrder(order);
        if (SellSignal.ECOMMERCE_WEB.name().equals(order.getSellSignal())) {
            ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_TO_RETAIL_STATUS_ECOMMERCE_WEB;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        String lockKey = "changeOrderToRetail:" + orderCode;
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            if (!OrderType.ONLINE.toString().equals(order.getType())) {
                ErrorCodes err = ErrorCodes.ONLY_ONLINE_TYPE_CAN_CHANGE_TO_RETAIL;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            String orderStatus = order.getOrderStatus();
            OrderStatus oldStatus = OrderStatus.findByCode(orderStatus);

            if (mustCheckPermissionOf(oldStatus)) {
                boolean hasPermission = checkPermissionToChangeCompletedOrder(order);
                if (!hasPermission) {
                    ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_COMPLETED_STATUS;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus());
                }
            }

            CommerceChangeOrderStatusParameter parameter = new CommerceChangeOrderStatusParameter(order, oldStatus,
                    OrderStatus.CHANGE_TO_RETAIL);
            CommerceChangeOrderStatusModification modification = commerceChangeOrderStatusStrategy.changeToHigherStatus(parameter);
            order.setOrderStatus(OrderStatus.CHANGE_TO_RETAIL.code());
            OrderModel savedOrderModel = orderService.save(order);
            applicationEventPublisher.publishEvent(new OrderEvent(savedOrderModel));
            OrderHistoryModel orderHistoryModel = new OrderHistoryModel();
            orderHistoryModel.setPreviousStatus(oldStatus.code());
            orderHistoryModel.setCurrentStatus(OrderStatus.CHANGE_TO_RETAIL.code());
            orderHistoryModel.setOrder(savedOrderModel);
            OrderHistoryModel saveOrderHistoryModel = orderHistoryService.save(orderHistoryModel);
            applicationEventPublisher.publishEvent(new OrderHistoryEvent(saveOrderHistoryModel));
            return modification.getRetailOrderCode();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void holdingProductOfOrder(HoldingProductRequest request) {
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(request.getOrderCode(),
                request.getCompanyId(), false);
        validateAbstractOrder(orderModel);
        String lockKey = "holdingProducts:" + orderModel.getCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            commerceCartService.holdingProductOfOrder(request, orderModel);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(String orderCode, Long companyId) {
        OrderModel ordermodel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        if (ordermodel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (!OrderStatus.NEW.code().equals(ordermodel.getOrderStatus())) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (SellSignal.ECOMMERCE_WEB.name().equals(ordermodel.getSellSignal())) {
            ErrorCodes err = ErrorCodes.CAN_NOT_REMOVE_ECOMMERCE_WEB;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (OrderType.ONLINE.toString().equals(ordermodel.getType())) {
            List<InvoiceData> invoiceData = invoiceService.findAllOrderInvoices(companyId, orderCode, null, ordermodel.getType());
            List<InvoiceData> invoiceVerified = invoiceData.stream().filter(i -> BillStatus.VERIFIED.code().equals(i.getStatus())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(invoiceVerified)) {
                ErrorCodes err = ErrorCodes.INVALID_ORDER_INVOICE_VERIFIED;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            if (CommonUtils.readValue(ordermodel.getRedeemAmount()) > 0) {
                loyaltyService.cancelPendingRedeem(ordermodel);
            }
        }
        commerceCartService.removeOrder(ordermodel);
    }

    @Override
    public void updateWeightForOrderEntry(OrderEntryDTO orderEntryDTO) {
        OrderModel order = orderService.findByCodeAndCompanyId(orderEntryDTO.getOrderCode(), orderEntryDTO.getCompanyId());
        validateAbstractOrder(order);
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(order);
        parameter.setWeight(orderEntryDTO.getWeight());
        parameter.setEntryId(orderEntryDTO.getEntryId());
        commerceCartService.updateWeightForOrderEntry(parameter);

    }

    @Override
    public void updateNoteInOrder(NoteRequest noteRequest) {
        OrderModel order = orderService.findByCodeAndCompanyId(noteRequest.getOrderCode(), noteRequest.getCompanyId());
        if (order == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        commerceCartService.updateNoteInOrder(order, noteRequest);
    }

    @Override
    public void updateHoldingProductBy(String orderCode, Long entryId, HoldingData holdingData) {
        OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, holdingData.getCompanyId(), false);
        validateAbstractOrder(order);
        OrderEntryModel entryModel = orderService.findEntryBy(entryId, order);
        if (entryModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        String lockKey = "updateHoldingProducts:" + order.getCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            commerceCartService.updateHolingProduct(order, entryModel, holdingData);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderData applyCoupon(AppliedCouponRequest appliedCouponRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(appliedCouponRequest.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(appliedCouponRequest.getOrderCode(),
                    appliedCouponRequest.getCompanyId(), false);
            validateAbstractOrder(order);
            validateChangeCouponOf(order);
            CommerceRedeemCouponParameter parameter = new CommerceRedeemCouponParameter(order, appliedCouponRequest.getCouponCode());
            parameter.setRedemptionQuantity(appliedCouponRequest.getRedemptionQuantity());
            couponService.redeemCoupon(parameter);
            return orderConverter.convert(order);
        }
    }

    @Override
    public OrderData removeCoupon(AppliedCouponRequest appliedCouponRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(appliedCouponRequest.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(appliedCouponRequest.getOrderCode(),
                    appliedCouponRequest.getCompanyId(), false);
            validateAbstractOrder(order);
            validateChangeCouponOf(order);
            CommerceRedeemCouponParameter parameter = new CommerceRedeemCouponParameter(order, appliedCouponRequest.getCouponCode());
            couponService.releaseCoupon(parameter);
            return orderConverter.convert(order);
        }
    }

    @Override
    public OrderData addProductToCombo(AddSubOrderEntryRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(request.getOrderCode(),
                    request.getCompanyId(), false);
            validateAbstractOrder(order);
            validateUpdateComboEntryOf(order);
            ComboData comboData = productService.getCombo(request.getComboId(), request.getCompanyId());
            OrderEntryModel entryModel = orderService.findEntryBy(request.getEntryId(), order);
            validateAbstractOrderEntry(entryModel);
            ProductInComboData productInComboData = getValidatedProductInEntryCombo(request, comboData, entryModel);
            productInComboData.setQuantity(request.getQuantity());
            productInComboData.setUpdateQuantity(request.isUpdateQuantity());
            productInComboData.setComboGroupNumber(request.getComboGroupNumber());
            CommerceAbstractOrderEntryParameter parameter = new CommerceAbstractOrderEntryParameter(entryModel, order);
            parameter.setProductInComboData(productInComboData);
            parameter.setComboData(comboData);
            CommerceCartModification modification = commerceCartService.addProductToComboInOrder(parameter);
            OrderEntryData updatedEntry = orderEntryConverter.convert(modification.getEntry());
            OrderData orderData = orderConverter.convert(order);
            orderData.setUpdatedOrderEntry(updatedEntry);
            return orderData;
        }
    }

    private void validateUpdateComboEntryOf(OrderModel order) {
        if (!OrderType.ONLINE.toString().equals(order.getType())) {
            return;
        }

        OrderStatus orderStatus = OrderStatus.findByCode(order.getOrderStatus());
        boolean canUpdateInformationOrderConfirm = permissionFacade.hasPermission(PermissionCodes.CAN_UPDATE_INFORMATION_ORDER_CONFIRM.code(), order.getCompanyId());
        if (orderStatus == null || orderStatus.value() > OrderStatus.RETURNING.value() || (OrderStatus.CONFIRMED.value() <= orderStatus.value() && OrderStatus.RETURNING.value() <= orderStatus.value() && !canUpdateInformationOrderConfirm)) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_CHANGE_ORDER_ENTRY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public OrderData addComboToOrderIndirectly(AddSubOrderEntryRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            CommerceAbstractOrderParameter cartParameter = commerceSubOrderEntryParameterConverter.convert(request);
            validateUpdateComboEntryOf((OrderModel) cartParameter.getOrder());
            commerceCartService.changeOrderEntryToComboEntry(cartParameter);
            OrderModel order = (OrderModel) cartParameter.getOrder();
            sendReturnOrdersKafkaAndEvent(order);
            return orderConverter.convert(order);
        }
    }

    @Override
    public void removeSubEntry(RemoveSubOrderEntryRequest request) {
        OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(request.getOrderCode(),
                request.getCompanyId(), false);
        validateAbstractOrder(order);
        validateUpdateComboEntryOf(order);
        OrderEntryModel entryModel = orderService.findEntryBy(request.getEntryId(), order);
        validateAbstractOrderEntry(entryModel);
        if (ComboType.FIXED_COMBO.toString().equals(entryModel.getComboType())) {
            ErrorCodes err = ErrorCodes.CANNOT_REMOVE_SUB_ORDER_ENTRY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        SubOrderEntryModel subOrderEntryModel = subOrderEntryService.findByOrderEntryAndId(entryModel, request.getSubEntryId());
        if (subOrderEntryModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_SUB_ORDER_ENTRY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        commercePlaceOrderStrategy.deleteProductOfComboInReturnBillWithOrder(order, entryModel, subOrderEntryModel);
        revertHoldingOrPreOrderProductInCombo(order, entryModel, subOrderEntryModel);

        if (CollectionUtils.isEmpty(entryModel.getSubOrderEntries())) {
            commercePlaceOrderStrategy.revertComboSaleQuantity(request.getCompanyId(), entryModel);
        }
    }

    private void revertHoldingOrPreOrderProductInCombo(OrderModel order, OrderEntryModel entryModel, SubOrderEntryModel subOrderEntryModel) {
        if (!OrderType.ONLINE.name().equals(order.getType())) return;
        UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
        data.setProductId(subOrderEntryModel.getProductId());
        data.setValue(subOrderEntryModel.getQuantity().longValue());
        OrderStatus orderStatus = OrderStatus.findByCode(order.getOrderStatus());
        boolean holding = entryModel.isHolding();
        if (OrderStatus.CONFIRMED.value() <= orderStatus.value() && orderStatus.value() < OrderStatus.SHIPPING.value()) {
            holding = true;
        }

        if (holding) {
            inventoryService.updateStockHoldingProductOfList(order, Arrays.asList(data), false);
        }

        if (entryModel.isPreOrder()) {
            inventoryService.updatePreOrderProductOfList(order, Arrays.asList(data), false);
        }
    }

    @Override
    @Transactional
    public <T extends AbstractOrderData> T importOrderItem(String orderCode, Long companyId, MultipartFile multipartFile) {
        OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateAbstractOrder(order);
        validateImportOrderItem(order);
        List<OrderItemDTO> orderItemDTOList = orderItemExcelFileReader.read(multipartFile);
        if (CollectionUtils.isEmpty(orderItemDTOList)) {
            ErrorCodes err = ErrorCodes.EMPTY_IMPORT_ORDER_PRODUCT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        importOrderItemValidator.validate(orderItemDTOList);
        List<OrderItemDTO> errorItems = orderItemDTOList.stream().filter(item -> StringUtils.isNotBlank(item.getError()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorItems)) {
            OrderImportData orderImportData = createErrorOrderImportData(errorItems);
            return (T) orderImportData;
        }
        AbstractOrderItemImportParameter param = new AbstractOrderItemImportParameter(orderItemDTOList);
        orderEntriesPopulator.populate(param, order);
        errorItems = orderItemDTOList.stream().filter(item -> StringUtils.isNotBlank(item.getError()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorItems)) {
            OrderImportData orderImportData = createErrorOrderImportData(errorItems);
            return (T) orderImportData;
        }

        commerceCartService.recalculate(order, true);
        applicationEventPublisher.publishEvent(new OrderEvent(order));
        return (T) orderConverter.convert(order);
    }

    @Override
    public Map<Long, OrderSaleData> getSaleQuantity(SaleQuantityRequest request) {
        Map<Long, OrderSaleData> orderSaleResult = new HashMap<>();
        List<SaleQuantity> orderEntries = orderService.findAllSaleEntryBy(request);

        for (SaleQuantity entry : orderEntries) {
            Long productId = entry.getProductId();
            OrderSaleData orderSaleData = orderSaleResult.get(productId);
            orderSaleData = orderSaleData == null ? new OrderSaleData() : orderSaleData;
            String orderType = entry.getOrderType();
            long saleQuantity = entry.getQuantity() == null ? 0 : entry.getQuantity();
            populateSaleData(orderSaleData, orderType, saleQuantity);

            orderSaleResult.put(productId, orderSaleData);
        }

        List<SaleQuantity> subOrderEntryModels = orderService.findAllSaleComboEntries(request);
        for (SaleQuantity comboEntrySaleQuantity : subOrderEntryModels) {
            Long productId = comboEntrySaleQuantity.getProductId();
            OrderSaleData orderSaleData = orderSaleResult.get(productId);
            orderSaleData = orderSaleData == null ? new OrderSaleData() : orderSaleData;
            String orderType = comboEntrySaleQuantity.getOrderType();
            long saleQuantity = CommonUtils.readValue(comboEntrySaleQuantity.getQuantity());
            populateSaleData(orderSaleData, orderType, saleQuantity);
            orderSaleResult.put(productId, orderSaleData);
        }

        return orderSaleResult;
    }

    private void populateSaleData(OrderSaleData orderSaleData, String orderType, long saleQuantity) {
        if (OrderType.ONLINE.toString().equals(orderType)) {
            orderSaleData.setOnline(orderSaleData.getOnline() + saleQuantity);
        } else if (OrderType.WHOLESALE.toString().equals(orderType)) {
            orderSaleData.setWholesale(orderSaleData.getWholesale() + saleQuantity);
        } else if (OrderType.RETAIL.toString().equals(orderType)) {
            orderSaleData.setRetail(orderSaleData.getRetail() + saleQuantity);
        }
    }

    @Override
    @Transactional
    public OrderData appliedPromotion(String orderCode, Long companyId, Long promotionSourceRuleId) {
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateAbstractOrder(orderModel);
        orderModel.setAppliedPromotionSourceRuleId(promotionSourceRuleId);
        commerceCartService.recalculate(orderModel, true);
        loyaltyService.updateRewardRedeemForOrder(orderModel);
        applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
        return orderConverter.convert(orderModel);
    }

    @Override
    public OrderData updateToppingOption(ToppingOptionRequest request, String orderCode) {
        Mutex<String> mutex = mutexFactory.getMutex(orderCode);
        synchronized (mutex) {
            OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, request.getCompanyId(), false);
            validateAbstractOrder(orderModel);
            OrderEntryModel entryModel = orderService.findEntryBy(request.getEntryId(), orderModel);
            validateAbstractOrderEntry(entryModel);
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updateToppingOptionOrder", orderCode,
                    request.getId(), request.getTimeRequest())) {
                return orderConverter.convert(orderModel);
            }
            ToppingOptionParameter parameter = new ToppingOptionParameter();
            parameter.setId(request.getId());
            parameter.setAbstractOrderModel(orderModel);
            parameter.setAbstractOrderEntryModel(entryModel);
            parameter.setQuantity(request.getQuantity());
            parameter.setIce(request.getIce());
            parameter.setSugar(request.getSugar());
            commerceCartService.updateOrderToppingOption(parameter);
            return orderConverter.convert(orderModel);
        }
    }

    @Override
    public OrderData removeToppingOptions(String orderCode, Long entryId, Long toppingOptionId, Long companyId) {
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateAbstractOrder(orderModel);
        OrderEntryModel orderEntryModel = orderService.findEntryBy(entryId, orderModel);
        validateAbstractOrderEntry(orderEntryModel);

        ToppingOptionParameter parameter = new ToppingOptionParameter();
        parameter.setAbstractOrderModel(orderModel);
        parameter.setAbstractOrderEntryModel(orderEntryModel);
        parameter.setId(toppingOptionId);
        commerceCartService.deleteToppingOptionInOrder(parameter);
        return orderConverter.convert(orderModel);
    }

    @Override
    public OrderData removeToppingItems(String orderCode, Long entryId, Long optionId, Long toppingItemId, Long companyId) {
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateAbstractOrder(orderModel);
        OrderEntryModel orderEntryModel = orderService.findEntryBy(entryId, orderModel);
        validateAbstractOrderEntry(orderEntryModel);
        ToppingOptionModel toppingOptionModel = toppingOptionService.findByIdAndOrderEntry(optionId, orderEntryModel);
        validateToppingOptionModel(toppingOptionModel);

        ToppingItemParameter parameter = new ToppingItemParameter();
        parameter.setAbstractOrderModel(orderModel);
        parameter.setAbstractOrderEntryModel(orderEntryModel);
        parameter.setToppingItemId(toppingItemId);
        parameter.setToppingOptionModel(toppingOptionModel);
        parameter.setQuantity(0);

        commerceCartService.updateOrderToppingItem(parameter);
        return orderConverter.convert(orderModel);
    }

    @Override
    public OrderData updateQuantityToppingItems(ToppingItemRequest request, String orderCode) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, request.getCompanyId(), false);
            validateAbstractOrder(orderModel);
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updateQuantityToppingItemsOrder", request.getOrderCode(),
                    request.getId(), request.getTimeRequest())) {
                return orderConverter.convert(orderModel);
            }

            AbstractOrderEntryModel orderEntryModel = orderService.findEntryBy(request.getEntryId(), orderModel);
            ToppingOptionModel toppingOptionModel = toppingOptionService.findByIdAndOrderEntry(request.getToppingOptionId(), orderEntryModel);
            validateToppingOptionModel(toppingOptionModel);
            ToppingItemParameter parameter = new ToppingItemParameter();
            parameter.setAbstractOrderModel(orderModel);
            parameter.setAbstractOrderEntryModel(orderEntryModel);
            parameter.setToppingOptionModel(toppingOptionModel);
            parameter.setQuantity(request.getQuantity());
            parameter.setToppingItemId(request.getId());
            commerceCartService.updateOrderToppingItem(parameter);
            return orderConverter.convert(orderModel);
        }
    }

    @Override
    public OrderData updateDiscountForToppingItem(ToppingItemRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(request.getOrderCode(), request.getCompanyId(), false);
            validateAbstractOrder(orderModel);
            CurrencyType currencyType = CurrencyType.findByCode(request.getDiscountType());
            validateCurrencyType(currencyType);
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updateDiscountForToppingItemCart", request.getOrderCode(),
                    request.getId(), request.getTimeRequest())) {
                return orderConverter.convert(orderModel);
            }
            OrderEntryModel cartEntryModel = orderService.findEntryBy(request.getEntryId(), orderModel);
            validateAbstractOrderEntry(cartEntryModel);
            ToppingItemParameter parameter = poulateToppingItemParameter(orderModel, cartEntryModel, request);
            parameter.setDiscountType(request.getDiscountType());
            parameter.setDiscount(request.getDiscount());
            commerceCartService.updateDiscountForToppingItem(parameter);
            return orderConverter.convert((OrderModel) parameter.getAbstractOrderModel());
        }
    }

    @Override
    public void createOrUpdateInvoices(InvoiceOrderRequest invoiceOrderRequest) {
        Pageable pageable = PageRequest.of(0, 100, Sort.Direction.ASC, "createdTime");
        while (true) {
            Page<OrderModel> orderPage = orderService.findAllByCompanyIdAndCreateTime(invoiceOrderRequest.getCompanyId(),
                    invoiceOrderRequest.getFromDate(), invoiceOrderRequest.getToDate(), pageable);
            List<OrderModel> orders = orderPage.getContent();
            if (CollectionUtils.isEmpty(orders)) {
                LOGGER.info("FINISH CREATE INVOICE: {}", orderPage.getTotalElements());
                break;
            }

            for (OrderModel order : orders) {
                invoiceService.saveInvoices(order, order.getCustomerId());
            }

            pageable = pageable.next();
        }
    }

    @Override
    public OrderData addToppingOptionsToOrder(ToppingOptionRequest request, String orderCode) {
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, request.getCompanyId(), false);
        validateAbstractOrder(orderModel);
        OrderEntryModel entryModel = orderService.findEntryBy(request.getEntryId(), orderModel);
        validateAbstractOrderEntry(entryModel);
        ToppingOptionParameter parameter = new ToppingOptionParameter();
        parameter.setAbstractOrderModel(orderModel);
        parameter.setAbstractOrderEntryModel(entryModel);
        parameter.setQuantity(request.getQuantity());
        parameter.setIce(request.getIce());
        parameter.setSugar(request.getSugar());
        commerceCartService.addToppingOption(parameter);
        return orderConverter.convert(orderModel);
    }

    @Override
    public OrderData addToppingItems(String orderCode, ToppingItemRequest request) {
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, request.getCompanyId(), false);
        validateAbstractOrder(orderModel);
        ToppingItemParameter parameter = new ToppingItemParameter();
        toppingItemOrderParameterPopulator.populate(request, parameter);
        commerceCartService.addToppingItem(parameter);
        return orderConverter.convert(orderModel);
    }

    @Override
    @Transactional
    public void updatePaymentTransactionDataAndPaidAmount(InvoiceKafkaData invoiceKafkaData) {
        OrderModel model = orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(
                invoiceKafkaData.getReferId(),
                invoiceKafkaData.getCompanyId(),
                OrderType.ONLINE.toString(),
                false);

        if (model == null) {
            LOGGER.warn("NOT FOUND ONLINE ORDER, referOrderCode: {}", invoiceKafkaData.getReferId());
            return;
        }

        if (PaymentMethodType.LOYALTY_POINT.code().equals(invoiceKafkaData.getPaymentMethodCode())) {
            paymentTransactionService.updatePaymentByInvoice(model, invoiceKafkaData);
        }

        if (!BillStatus.VERIFIED.code().equals(invoiceKafkaData.getStatus())
                && !PaymentMethodType.LOYALTY_POINT.code().equals(invoiceKafkaData.getPaymentMethodCode())) {
            paymentTransactionService.removePaymentByInvoice(model, invoiceKafkaData.getInvoiceId());
        }

        model.setPaidAmount(invoiceKafkaData.getFinalAmount());
        OrderModel savedOrderModel = orderService.save(model);
        orderElasticSearchService.updatePaymentTransactionDataAndPaidAmount(savedOrderModel, invoiceKafkaData);
    }

    @Override
    public void updatePaidAmountAllOrder(OrderPartialIndexRequest request) {
        LOGGER.info("=======  start update paid amount all order");
        if (request.getOrderId() != null) {
            OrderModel orderModel = orderService.findById(request.getOrderId());
            if (orderModel != null) {
                orderService.updatePaidAmountOrder(orderModel);
            }
        } else {
            sendCalculatePaidAmountOrderKafka(request);
        }
    }

    private void sendCalculatePaidAmountOrderKafka(OrderPartialIndexRequest request) {
        if (request.getCompanyId() == null) {
            ErrorCodes errorCodes = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(errorCodes.code(), errorCodes.message(), errorCodes.httpStatus());
        }
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            Pageable pageable = PageRequest.of(0, 300);
            while (true) {
                Page<OrderModel> orderPage;
                if (request.getFromDate() != null) {
                    orderPage = orderService.findAllByCompanyIdAndTypeAndFromDate(pageable, request.getCompanyId(), OrderType.ONLINE.name(), request.getFromDate());
                } else {
                    orderPage = orderService.findAllByCompanyIdAndType(pageable, request.getCompanyId(), OrderType.ONLINE.name());
                }
                List<OrderModel> orders = orderPage.getContent();
                if (CollectionUtils.isEmpty(orders)) {
                    LOGGER.info("FINISH UPDATE_PAID_AMOUNT: {}", orderPage.getTotalElements());
                    break;
                }

                calculatePaidAmountOrderProducer.produce(orders);
                pageable = pageable.next();
            }
        });
    }

    @Override
    @Transactional
    public OrderData refresh(RefreshCartRequest refreshCartRequest) {
        OrderModel order = orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted(refreshCartRequest.getCode(),
                refreshCartRequest.getOldCompanyId(), OrderType.ONLINE.toString(), false);
        validateAbstractOrder(order);
        OrderStatus status = OrderStatus.findByCode(order.getOrderStatus());
        if (status == null || status.value() >= OrderStatus.PRE_ORDER.value()) {
            ErrorCodes errorCodes = ErrorCodes.CAN_NOT_CHANGE_WAREHOUSE_OR_COMPANY;
            throw new ServiceException(errorCodes.code(), errorCodes.message(), errorCodes.httpStatus());
        }

        if (!refreshCartRequest.getCompanyId().equals(order.getCompanyId())) {
            order.setDistributorId(null);
        }
        if (!shouldClearCartData(order, refreshCartRequest)) {
            if (CollectionUtils.isNotEmpty(order.getPaymentTransactions())) {
                for (PaymentTransactionModel paymentTransactionModel : order.getPaymentTransactions()) {
                    paymentTransactionModel.setWarehouseId(order.getWarehouseId());
                }
            }
            order = orderService.save(order);
            invoiceService.saveInvoices(order, order.getCustomerId());
            return orderConverter.convert(order);
        }
        invoiceService.unverifyInvoices(order);
        if (CollectionUtils.isNotEmpty(order.getPaymentTransactions())) {
            order.getPaymentTransactions().clear();
        }
        order.setCustomerId(null);
        order = orderService.save(order);

        if (CollectionUtils.isNotEmpty(order.getEntries())) {
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            commerceCartService.removeAllEntries(parameter);
        }
        applicationEventPublisher.publishEvent(new OrderEvent(order));
        return orderConverter.convert(order);

    }

    @Override
    public void updateComboReport(Long companyId) {
        List<OrderModel> orderModels = orderService.findOrderCombo(companyId);
        for (OrderModel orderModel : orderModels) {
            OrderEvent event = new OrderEvent(orderModel);
            producerService.sendOrderKafka(event);
        }
    }

    @Override
    public OrderData removeListEntry(EntryRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(request.getOrderCode(), request.getCompanyId(), false);
            validateAbstractOrder(order);
            commerceCartService.updateListOrderEntry(order, request);
            sendReturnOrdersKafkaAndEvent(order);
            return orderConverter.convert(order);
        }
    }

    @Override
    public void linkOrderToBill(Long companyId) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            Pageable pageable = PageRequest.of(0, 300);
            while (true) {
                Page<OrderModel> orderModelPage = orderService.findAllByCompanyId(companyId, pageable);
                List<OrderModel> orders = orderModelPage.getContent();
                if (CollectionUtils.isEmpty(orders)) {
                    break;
                }
                orderBillLinkProducer.produce(orders);
                pageable = pageable.next();
            }
            LOGGER.info("FINISHED LINK ORDER TO BILL");
        });
    }

    @Override
    public OrderData updatePriceForOrderEntries(OrderRequest orderRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(orderRequest.getCode());
        synchronized (mutex) {
            OrderModel orderModel = orderService.findByCodeAndCompanyId(orderRequest.getCode(), orderRequest.getCompanyId());
            validateAbstractOrder(orderModel);
            orderModel.setDistributorId(orderRequest.getDistributorId());
            orderModel.setPriceType(orderRequest.getPriceType());

            validateDistributorPriceOrder(orderModel);
            if (CollectionUtils.isEmpty(orderModel.getEntries())) {
                orderService.save(orderModel);
                return orderConverter.convert(orderModel);
            }
            commerceCartService.updatePriceForCartEntries(orderModel);
            return orderConverter.convert(orderModel);
        }
    }

    @Override
    public void updateOrderSourceForReturnOrder(com.vctek.kafka.data.OrderData orderData) {
        if (!OrderType.ONLINE.toString().equals(orderData.getOrderType()) || orderData.isExchange()) {
            return;
        }

        String orderCode = orderData.getOrderCode();
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, orderData.getCompanyId());
        if (orderModel == null) {
            return;
        }

        List<ReturnOrderModel> returnOrderModels = returnOrderService.findAllByOriginOrder(orderModel);
        if (CollectionUtils.isEmpty(returnOrderModels)) {
            return;
        }
        List<OrderModel> updateExchangeOrders = new ArrayList<>();
        OrderSourceModel orderSourceModel = orderModel.getOrderSourceModel();
        for (ReturnOrderModel model : returnOrderModels) {
            OrderModel exchangeOrder = model.getExchangeOrder();
            if (exchangeOrder != null) {
                exchangeOrder.setOrderSourceModel(orderSourceModel);
                updateExchangeOrders.add(exchangeOrder);
            }
        }

        orderService.saveAll(updateExchangeOrders);
        for (ReturnOrderModel model : returnOrderModels) {
            updateReturnOrderProducer.sendRequestUpdateReturnOrder(model);
        }
    }

    private void validateImportOrderItem(OrderModel order) {
        if (!OrderType.ONLINE.toString().equals(order.getType())) {
            ErrorCodes err = ErrorCodes.CANNOT_IMPORT_FOR_NOT_ONLINE_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderStatus orderStatus = OrderStatus.findByCode(order.getOrderStatus());
        if (orderStatus == null || OrderStatus.CONFIRMING.value() < orderStatus.value()) {
            ErrorCodes err = ErrorCodes.CANNOT_IMPORT_FOR_ONLINE_ORDER_STATUS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validateChangeCouponOf(OrderModel order) {
        if (OrderType.ONLINE.toString().equals(order.getType())) {
            OrderStatus currentOrderStatus = OrderStatus.findByCode(order.getOrderStatus());
            if (currentOrderStatus != null && currentOrderStatus.value() > OrderStatus.CONFIRMED.value()) {
                ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_COUPON_CODE_OF_ONLINE_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    @Override
    public OrderData updateShippingFee(OrderRequest orderRequest) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderRequest.getCode(), orderRequest.getCompanyId());
        if (orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (!OrderType.ONLINE.toString().equals(orderModel.getType())) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_TYPE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        orderModel.setDeliveryCost(orderRequest.getDeliveryCost());
        orderModel.setCompanyShippingFee(orderRequest.getCompanyShippingFee());
        orderModel.setCollaboratorShippingFee(orderRequest.getCollaboratorShippingFee());
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(orderModel);

        commerceCartService.updateShippingFee(parameter);
        return orderConverter.convert(orderModel);
    }

    @Override
    public OrderData updateDefaultSettingCustomer(OrderRequest orderRequest) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderRequest.getCode(), orderRequest.getCompanyId());
        if (orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        orderModel.setAge(orderRequest.getAge());
        orderModel.setGender(orderRequest.getGender());
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(orderModel);

        commerceCartService.updateDefaultSettingCustomer(parameter);
        return orderConverter.convert(orderModel);
    }

    @Override
    public List<OrderSettingDiscountData> checkDiscountMaximum(Long companyId, String cartCode) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(cartCode, companyId);

        validateAbstractOrder(orderModel);
        if (orderModel.getConfirmDiscountBy() != null) {
            return new ArrayList<>();
        }
        return commerceCartService.checkDiscountMaximumOrder(orderModel);
    }

    @Override
    public OrderData updateAllDiscountForOrder(String orderCode, UpdateAllDiscountRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(orderCode);
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, request.getCompanyId(), false);
            validateAbstractOrder(order);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            commerceCartService.updateAllDiscountForCart(parameter, request);
            return orderConverter.convert(order);
        }
    }

    @Override
    public OrderData findOrderByExternalId(CartInfoParameter cartInfoParameter) {
        OrderModel model = orderService.findOrderByExternalIdAndSellSignal(cartInfoParameter);
        if (model != null) {
            return orderConverter.convert(model);
        }

        return new OrderData();
    }

    @Override
    @Transactional
    public void uploadImageToOrder(OrderImagesRequest request, String orderCode) {
        OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, request.getCompanyId(), false);
        List<String> orderStatusNew = Arrays.asList(
                OrderStatus.NEW.code(),
                OrderStatus.CONFIRMING.code(),
                OrderStatus.CONFIRMING_CHANGE.code(),
                OrderStatus.PRE_ORDER.code()
        );

        if (!orderStatusNew.contains(order.getOrderStatus())) {
            boolean checkUpdate = permissionFacade.checkPermission(PermissionCodes.UPDATE_ORDER.code(),
                    authService.getCurrentUserId(), order.getCompanyId());
            if (Boolean.FALSE.equals(checkUpdate)) {
                ErrorCodes err = ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
        validateAbstractOrder(order);
        if (CollectionUtils.isEmpty(request.getOrderImages())) {
            return;
        }

        try {
            order.setImages(objectMapper.writeValueAsString(request.getOrderImages()));
        } catch (JsonProcessingException e) {
            LOGGER.error("CANNOT WRITE ORDER IMAGE: {}", orderCode);
        }
        OrderModel saveOrder = orderService.save(order);
        applicationEventPublisher.publishEvent(new OrderEvent(saveOrder));
    }

    @Override
    public OrderData updateRecommendedRetailPriceForOrderEntry(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyId(orderEntryDTO.getOrderCode(), orderEntryDTO.getCompanyId());
            validateAbstractOrder(order);
            if (!updateOrderSequenceCacheService.isValidTimeRequest("updateRecommendedRetailPriceForOrderEntry", orderEntryDTO.getOrderCode(),
                    orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                return orderConverter.convert(order);
            }

            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            parameter.setEntryId(orderEntryDTO.getEntryId());
            parameter.setRecommendedRetailPrice(orderEntryDTO.getRecommendedRetailPrice());
            boolean reload = commerceCartService.updateRecommendedRetailPriceForCartEntry(parameter);
            OrderData orderData = orderConverter.convert(order);
            orderData.setReload(reload);
            return orderData;
        }
    }

    @Override
    public OrderData cancelRedeem(String orderCode, Long companyId) {
        Mutex<String> mutex = mutexFactory.getMutex(orderCode);
        synchronized (mutex) {
            OrderModel model = orderService.findByCodeAndCompanyId(orderCode, companyId);
            validateAbstractOrder(model);
            validateUpdateRedeemOnline(model);
            if (model.getRedeemAmount() != null) {
                OrderModel newModel = commercePlaceOrderStrategy.cancelRedeem(model);
                return orderConverter.convert(newModel);
            }
            return orderConverter.convert(model);
        }
    }

    @Override
    public double updateRedeemOnline(String orderCode, Long companyId, PaymentTransactionRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(orderCode);
        synchronized (mutex) {
            OrderModel model = orderService.findByCodeAndCompanyId(orderCode, companyId);
            validateAbstractOrder(model);
            validateUpdateRedeemOnline(model);
            return commercePlaceOrderStrategy.updateRedeemOnline(model, request);
        }
    }

    @Override
    public double createRedeemOnline(String orderCode, Long companyId, PaymentTransactionRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(orderCode);
        synchronized (mutex) {
            OrderModel model = orderService.findByCodeAndCompanyId(orderCode, companyId);
            validateAbstractOrder(model);
            validateUpdateRedeemOnline(model);
            return commercePlaceOrderStrategy.createRedeemOnline(model, request);
        }
    }

    private void validateUpdateRedeemOnline(OrderModel model) {
        if (model.getCustomerId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public AwardLoyaltyData getLoyaltyPointsFor(String orderCode, Long companyId) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, companyId);
        validateAbstractOrder(orderModel);
        return loyaltyService.getLoyaltyPointsOf(orderModel);
    }

    @Override
    public OrderData updateSettingCustomerToOrder(OrderRequest orderRequest) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderRequest.getCode(), orderRequest.getCompanyId());
        validateAbstractOrder(orderModel);
        commercePlaceOrderStrategy.updateSettingCustomerToOrder(orderModel, orderRequest.getSettingCustomerOptionIds());
        return orderConverter.convert(orderModel);
    }

    @Override
    public void addTag(AddTagRequest addTagRequest) {
        orderService.addTag(addTagRequest);
    }

    @Override
    public void removeTag(Long companyId, String orderCode, Long tagId) {
        if(companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        orderService.removeTag(companyId, orderCode, tagId);
    }

    @Override
    public OrderData markEntrySaleOff(EntrySaleOffRequest request) {
        if(request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            OrderModel order = orderService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
            validateAbstractOrder(order);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(order);
            parameter.setEntryId(request.getEntryId());
            parameter.setSaleOff(request.isSaleOff());
            saleOffOrderEntryValidator.validate(parameter);
            commerceCartService.markEntrySaleOff(parameter);
            return orderConverter.convert(order);
        }
    }

    @Override
    public boolean isSaleOffEntry(OrderEntryDTO orderEntryDTO) {
        return orderService.isSaleOffEntry(orderEntryDTO);
    }

    @Override
    public OrderData updateCustomer(UpdateCustomerRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getCode());
        synchronized (mutex) {
            validateCustomer(request.getCustomer(), request.getCompanyId());
            OrderModel orderModel = orderService.findByCodeAndCompanyId(request.getCode(), request.getCompanyId());
            validateAbstractOrder(orderModel);
            if ((orderModel.getRewardPoint() != null || orderModel.getRedeemAmount() != null) &&
                    StringUtils.isNotBlank(orderModel.getCardNumber()) && !orderModel.getCardNumber().equals(request.getCardNumber())) {
                ErrorCodes err = ErrorCodes.CANNOT_UPDATE_LOYALTY_CARD_INFO;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            AbstractOrderModel abstractOrderModel = commerceCartService.updateCustomer(request, orderModel);
            OrderModel order = (OrderModel) abstractOrderModel;
            return orderConverter.convert(order);
        }
    }

    @Override
    public OrderData addVAT(Long companyId, String orderCode, Boolean addVat) {
        Mutex<String> mutex = mutexFactory.getMutex(orderCode);
        synchronized (mutex) {
            OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, companyId);
            validateAbstractOrder(orderModel);
            AbstractOrderModel abstractOrderModel = commerceCartService.addVatOf(orderModel, addVat);
            OrderModel order = (OrderModel) abstractOrderModel;
            return orderConverter.convert(order);
        }
    }

    @Override
    public OrderData changeOrderSource(CartInfoParameter cartInfoParameter) {
        Mutex<String> mutex = mutexFactory.getMutex(cartInfoParameter.getCode());
        synchronized (mutex) {
            OrderModel orderModel = orderService.findByCodeAndCompanyId(cartInfoParameter.getCode(), cartInfoParameter.getCompanyId());
            validateAbstractOrder(orderModel);
            AbstractOrderModel abstractOrderModel = commerceCartService.changeOrderSource(orderModel, cartInfoParameter.getOrderSourceId());
            OrderModel order = (OrderModel) abstractOrderModel;
            return orderConverter.convert(order);
        }
    }

    @Autowired
    @Qualifier("commerceCheckoutParameterConverter")
    public void setCommerceCheckoutParameterConverter(Converter<OrderRequest, CommerceCheckoutParameter> commerceCheckoutParameterConverter) {
        this.commerceCheckoutParameterConverter = commerceCheckoutParameterConverter;
    }

    @Autowired
    public void setCommercePlaceOrderStrategy(CommercePlaceOrderStrategy commercePlaceOrderStrategy) {
        this.commercePlaceOrderStrategy = commercePlaceOrderStrategy;
    }

    @Autowired
    public void setOrderConverter(Converter<OrderModel, OrderData> orderConverter) {
        this.orderConverter = orderConverter;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    @Qualifier("commerceOrderParameterConverter")
    public void setCommerceOrderParameterConverter(Converter<OrderEntryDTO, CommerceAbstractOrderParameter> commerceOrderParameterConverter) {
        this.commerceOrderParameterConverter = commerceOrderParameterConverter;
    }

    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }

    @Autowired
    @Qualifier("updateOrderParameterConverter")
    public void setUpdateOrderParameterConverter(Converter<OrderRequest, UpdateOrderParameter> updateOrderParameterConverter) {
        this.updateOrderParameterConverter = updateOrderParameterConverter;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @Autowired
    public void setCommerceChangeOrderStatusStrategy(CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy) {
        this.commerceChangeOrderStatusStrategy = commerceChangeOrderStatusStrategy;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Autowired
    public void setRedisLockService(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    @Qualifier("commerceSubOrderEntryParameterConverter")
    public void setCommerceSubOrderEntryParameterConverter(Converter<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> commerceSubOrderEntryParameterConverter) {
        this.commerceSubOrderEntryParameterConverter = commerceSubOrderEntryParameterConverter;
    }

    @Autowired
    public void setSubOrderEntryService(SubOrderEntryRepository subOrderEntryService) {
        this.subOrderEntryService = subOrderEntryService;
    }

    @Autowired
    public void setProducerService(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    @Autowired
    public void setReturnOrdersProducerService(ReturnOrdersProducerService returnOrdersProducerService) {
        this.returnOrdersProducerService = returnOrdersProducerService;
    }

    @Autowired
    @Qualifier("toppingItemOrderParameterPopulator")
    public void setToppingItemOrderParameterPopulator(Populator<ToppingItemRequest, ToppingItemParameter> toppingItemOrderParameterPopulator) {
        this.toppingItemOrderParameterPopulator = toppingItemOrderParameterPopulator;
    }

    @Autowired
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Autowired
    public void setOrderElasticSearchService(OrderElasticSearchService orderElasticSearchService) {
        this.orderElasticSearchService = orderElasticSearchService;
    }

    @Autowired
    public void setOrderBillLinkProducer(OrderBillLinkProducer orderBillLinkProducer) {
        this.orderBillLinkProducer = orderBillLinkProducer;
    }

    @Autowired
    public void setCalculatePaidAmountOrderProducer(CalculatePaidAmountOrderProducer calculatePaidAmountOrderProducer) {
        this.calculatePaidAmountOrderProducer = calculatePaidAmountOrderProducer;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setUpdateOrderSequenceCacheService(UpdateOrderSequenceCacheService updateOrderSequenceCacheService) {
        this.updateOrderSequenceCacheService = updateOrderSequenceCacheService;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    public void setUpdateReturnOrderProducer(UpdateReturnOrderProducer updateReturnOrderProducer) {
        this.updateReturnOrderProducer = updateReturnOrderProducer;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    @Qualifier("saleOffOrderEntryValidator")
    public void setSaleOffOrderEntryValidator(Validator<CommerceAbstractOrderParameter> saleOffOrderEntryValidator) {
        this.saleOffOrderEntryValidator = saleOffOrderEntryValidator;
    }

    @Autowired
    @Qualifier("saleOffUpdateQuantityOrderEntryValidator")
    public void setSaleOffUpdateQuantityOrderEntryValidator(Validator<CommerceAbstractOrderParameter> saleOffUpdateQuantityOrderEntryValidator) {
        this.saleOffUpdateQuantityOrderEntryValidator = saleOffUpdateQuantityOrderEntryValidator;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }
}
