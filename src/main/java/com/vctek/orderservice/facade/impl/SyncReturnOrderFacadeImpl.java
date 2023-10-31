package com.vctek.orderservice.facade.impl;

import com.vctek.migration.dto.MigrateBillDto;
import com.vctek.orderservice.facade.SyncReturnOrderFacade;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.repository.dao.ReturnOrderDAO;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SyncReturnOrderFacadeImpl implements SyncReturnOrderFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncReturnOrderFacadeImpl.class);
    private ReturnOrderService returnOrderService;
    private ReturnOrderDAO returnOrderDAO;
    private OrderService orderService;
    private String note = "Migrate return order";

    @Override
    @Transactional
    public void processSyncReturnOrderMessage(MigrateBillDto data) {
        try {
            ReturnOrderModel returnOrderExist = returnOrderService.findByExportExternalIdAndCompanyId(data.getBillReturnId(), data.getCompanyId());
            if (returnOrderExist != null) {
                return;
            }
            OrderModel originOrder = orderService.findByCodeAndCompanyId(String.valueOf(CommonUtils.readValue(data.getParentId())), data.getCompanyId());
            OrderModel exchangeOrder = null;
            if (data.getBillExchangeId() != null) {
                 exchangeOrder = orderService.findByCodeAndCompanyId(data.getBillExchangeId().toString(), data.getCompanyId());
            }
            ReturnOrderModel returnOrderModel = populateReturnOrder(data, originOrder, exchangeOrder);
            ReturnOrderModel saveReturnOrder = returnOrderService.onlySave(returnOrderModel);
            returnOrderDAO.updateAuditing(saveReturnOrder, data);
            if (originOrder != null) {
                originOrder.getReturnOrders().add(saveReturnOrder);
                orderService.save(originOrder);
            }
            if (exchangeOrder != null) {
                exchangeOrder.setReturnOrder(saveReturnOrder);
                orderService.save(exchangeOrder);
            }

        } catch (RuntimeException e) {
            LOGGER.error("=======  SyncData return Order: fail: {}, error: {} ", data.getOrderCode(), e.getMessage());
            LOGGER.error(" error: {} ", e);
        }
    }

    private ReturnOrderModel populateReturnOrder(MigrateBillDto data, OrderModel originOrder, OrderModel exchangeOrder) {
        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        returnOrderModel.setCompanyId(data.getCompanyId());
        returnOrderModel.setNote(data.getNote());
        returnOrderModel.setOriginOrder(originOrder);
        if (returnOrderModel.getOriginOrder() == null) {
            OrderModel orderModel = new OrderModel();
            orderModel.setCompanyId(data.getCompanyId());
            orderModel.setOrderStatus(OrderStatus.SYSTEM_CANCEL.code());
            orderModel.setType(OrderType.ONLINE.name());
            orderModel.setCode(data.getId() + "_MIGRATION");
            orderModel.setDeleted(true);
            orderModel.setNote(note);
            returnOrderModel.setOriginOrder(orderModel);
        }
        if (exchangeOrder != null) {
            returnOrderModel.setExchangeOrder(exchangeOrder);
            returnOrderModel.setExchangeOrderCode(exchangeOrder.getCode());
        }
        returnOrderModel.setExportExternalId(data.getBillReturnId());
        returnOrderModel.setExternalId(data.getId());
        return returnOrderModel;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    public void setReturnOrderDAO(ReturnOrderDAO returnOrderDAO) {
        this.returnOrderDAO = returnOrderDAO;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
