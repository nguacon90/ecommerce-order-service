package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Populator;
import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.migration.dto.SyncOrderNoteData;
import com.vctek.orderservice.facade.SyncOrderFacade;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.dao.OrderDAO;
import com.vctek.orderservice.repository.dao.OrderNoteDAO;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.OrderNoteService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class SyncOrderFacadeImpl implements SyncOrderFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncOrderFacade.class);
    private OrderService orderService;
    private Populator<MigrateBillDto, OrderModel> syncOrderPopulator;
    private OrderDAO orderDAO;
    private CalculationService calculationService;
    private OrderNoteService orderNoteService;
    private OrderNoteDAO orderNoteDAO;
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;

    @Override
    @Transactional
    public void processSyncOrderMessage(MigrateBillDto data) {
        try {
            OrderModel orderExist = orderService.findByCodeAndCompanyIdAndDeleted(data.getOrderCode(), data.getCompanyId(), false);
            if (orderExist != null) {
                commerceCartCalculationStrategy.splitOrderPromotionToEntries(orderExist);
                orderExist.setShippingAddressId(data.getShippingAddressId());
                orderExist.setEmployeeId(data.getEmployeeId());
                orderExist.setCustomerId(data.getCustomerId());
                orderService.save(orderExist);
                orderDAO.updateAuditing(orderExist, data);
                return;
            }
            OrderModel orderModel = new OrderModel();
            syncOrderPopulator.populate(data, orderModel);
            calculationService.calculate(orderModel);
            OrderModel saveOrder = orderService.save(orderModel);
            this.commerceCartCalculationStrategy.splitOrderPromotionToEntries(saveOrder);
            orderDAO.updateAuditing(saveOrder, data);

            if (CollectionUtils.isNotEmpty(data.getOrderNotes())) {
                createdOrderNote(saveOrder, data.getOrderNotes());
            }
            if (data.getOrderRetailCode() != null) {
                createdRetail(saveOrder, data);
            }

        } catch (RuntimeException e) {
            LOGGER.error("=======  SyncData Order: fail: {}, error: {} ", data.getOrderCode(), e.getMessage());
            LOGGER.error("error: {} ", e);
        }
    }

    private void createdRetail(OrderModel orderModel, MigrateBillDto data) {
        OrderModel changeToRetail = SerializationUtils.clone(orderModel);
        changeToRetail.setId(null);
        changeToRetail.setCode(data.getOrderRetailCode());
        changeToRetail.setOrderStatus(OrderStatus.CHANGE_TO_RETAIL.code());
        changeToRetail.setDeliveryCost(0d);
        changeToRetail.setType(OrderType.RETAIL.name());
        changeToRetail.setOrderRetailCode(null);
        changeToRetail.setReturnOrders(new HashSet<>());
        changeToRetail.setCouldFirePromotions(new HashSet<>());
        changeToRetail.setOrderSourceModel(null);
        changeToRetail.setCouponRedemptionModels(new HashSet<>());
        List<AbstractOrderEntryModel> entryModelList = new ArrayList<>();
        for (AbstractOrderEntryModel entryModel : orderModel.getEntries()) {
            OrderEntryModel clonedOrderEntryModel = orderService.cloneOrderEntry(changeToRetail, entryModel);
            entryModelList.add(clonedOrderEntryModel);
            orderService.cloneSubOrderEntries(entryModel, clonedOrderEntryModel);
        }
        changeToRetail.setEntries(entryModelList);
        changeToRetail.setCouponRedemptionModels(new HashSet<>());

        Set<PaymentTransactionModel> newPaymentList = new HashSet<>();
        for (PaymentTransactionModel paymentTransactionModel : orderModel.getPaymentTransactions()) {
            PaymentTransactionModel transactionModel = SerializationUtils.clone(paymentTransactionModel);
            transactionModel.setId(null);
            transactionModel.setOrderModel(changeToRetail);
            newPaymentList.add(transactionModel);
        }
        changeToRetail.setPaymentTransactions(newPaymentList);
        changeToRetail.setOrderHistory(Collections.emptyList());
        changeToRetail.setOrderHasCouponCodeModels(new HashSet<>());
        changeToRetail.setPromotionResults(new HashSet<>());
        orderService.save(changeToRetail);
        orderDAO.updateAuditing(changeToRetail, data);
        if (CollectionUtils.isNotEmpty(data.getOrderNotes())) {
            createdOrderNote(changeToRetail, data.getOrderNotes());
        }
    }

    private void createdOrderNote(OrderModel orderModel, List<SyncOrderNoteData> syncOrderNoteDataList) {
        for (SyncOrderNoteData noteData : syncOrderNoteDataList) {
            OrderNoteModel orderNoteModel = new OrderNoteModel();
            orderNoteModel.setContent(noteData.getContent());
            orderNoteModel.setOrder(orderModel);
            orderNoteModel.setOrderCode(orderModel.getCode());
            orderNoteService.save(orderNoteModel);
            orderNoteDAO.updateAuditing(orderNoteModel, noteData);
        }
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setSyncOrderPopulator(Populator<MigrateBillDto, OrderModel> syncOrderPopulator) {
        this.syncOrderPopulator = syncOrderPopulator;
    }

    @Autowired
    public void setOrderDAO(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setOrderNoteService(OrderNoteService orderNoteService) {
        this.orderNoteService = orderNoteService;
    }

    @Autowired
    public void setOrderNoteDAO(OrderNoteDAO orderNoteDAO) {
        this.orderNoteDAO = orderNoteDAO;
    }

    @Autowired
    public void setCommerceCartCalculationStrategy(CommerceCartCalculationStrategy commerceCartCalculationStrategy) {
        this.commerceCartCalculationStrategy = commerceCartCalculationStrategy;
    }
}
