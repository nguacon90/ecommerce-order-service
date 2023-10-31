package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.dto.CheckCreateTransferWarehouseData;
import com.vctek.dto.request.CheckCreateTransferWarehouseRequest;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderStatusImport;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderStatusImportModelPopulator implements Populator<OrderStatusImportRequest, OrderStatusImportModel> {

    private OrderService orderService;
    private LogisticService logisticService;

    @Override
    public void populate(OrderStatusImportRequest request, OrderStatusImportModel model) {
        Long companyId = request.getCompanyId();
        String newOrderStatus = request.getOrderStatus();

        model.setCompanyId(companyId);
        model.setOrderStatus(newOrderStatus);
        List<OrderModel> orderModels = orderService.findByCompanyIdAndOrderCodeIn(request.getCompanyId(), request.getOrderCodes());
        List<CheckCreateTransferWarehouseRequest> validRequests = new ArrayList<>();
        for (OrderModel orderModel : orderModels) {
            CheckCreateTransferWarehouseRequest validRequest = new CheckCreateTransferWarehouseRequest();
            validRequest.setOrderCode(orderModel.getCode());
            validRequest.setOrderStatus(newOrderStatus);
            validRequest.setCurrentOrderStatus(orderModel.getOrderStatus());
            validRequest.setWarehouseId(orderModel.getWarehouseId());
            validRequests.add(validRequest);
        }
        CheckCreateTransferWarehouseRequest validCreateTransferWarehouseRequest = new CheckCreateTransferWarehouseRequest();
        validCreateTransferWarehouseRequest.setRequests(validRequests);
        validCreateTransferWarehouseRequest.setCompanyId(request.getCompanyId());
        Map<String, CheckCreateTransferWarehouseData> hasCreateTransferWarehouse = logisticService.checkValidCreateTransferWarehouse(validCreateTransferWarehouseRequest);
        List<OrderStatusImportDetailModel> detailModels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getOrderCodes())) {
            for (String orderCode : request.getOrderCodes()) {
                OrderStatusImportDetailModel detailModel = new OrderStatusImportDetailModel();
                detailModel.setOrderCode(orderCode);
                detailModel.setNewOrderStatus(newOrderStatus);
                detailModel.setStatus(OrderStatusImport.PROCESSING.toString());
                detailModel.setOrderStatusImportModel(model);

                validOrderCode(orderModels, detailModel, detailModels);
                validCreateTransferWarehouse(detailModel, hasCreateTransferWarehouse, orderModels);

                detailModels.add(detailModel);
            }
            List<OrderStatusImportDetailModel> errors = detailModels.stream().filter(i -> OrderStatusImport.ERROR.toString().equals(i.getStatus())).collect(Collectors.toList());
            if (errors.size() == detailModels.size()) {
                model.setStatus(OrderStatusImport.COMPLETED.name());
            }
            model.setOrderStatusImportDetailModels(detailModels);
        }
    }

    private void validCreateTransferWarehouse(OrderStatusImportDetailModel detailModel, Map<String, CheckCreateTransferWarehouseData> hasCreateTransferWarehouse, List<OrderModel> orderModels) {
        CheckCreateTransferWarehouseData data = hasCreateTransferWarehouse.get(detailModel.getOrderCode());
        if (data == null) return;
        if (data.isHasCreateTransferWarehouse()) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(ErrorCodes.HAS_NOT_CHANGE_STATUS_IN_SETTING.code());
        }

        Optional<OrderModel> orderModelOptional = orderModels.stream().filter(i ->i.getCode().equals(detailModel.getOrderCode())).findFirst();
        if (!orderModelOptional.isPresent()) return;
        OrderModel orderModel = orderModelOptional.get();
        if (!detailModel.getNewOrderStatus().equals(data.getOrderStatus())) return;
        if (Boolean.FALSE.equals(data.isDeliveryWarehouseActive())) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(ErrorCodes.INACTIVE_DELIVERY_WAREHOUSE.code());
        }
        try {
            logisticService.validateTransferLessZero(orderModel, data.getDeliveryWarehouseId());
        } catch (ServiceException e) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(e.getCode());
        }
    }

    private void validOrderCode(List<OrderModel> orderModels, OrderStatusImportDetailModel detailModel, List<OrderStatusImportDetailModel> detailModels) {
        Optional<OrderStatusImportDetailModel> optional = detailModels.stream().filter(i -> i.getOrderCode().equals(detailModel.getOrderCode())).findFirst();
        if (optional.isPresent()) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(ErrorCodes.DUPLICATE_ORDER_CODE_FOR_CHANGE_STATUS.code());
            return;
        }
        Optional<OrderModel> orderModel = orderModels.stream().filter(i -> i.getCode().equals(detailModel.getOrderCode())).findFirst();
        if (!orderModel.isPresent() || orderModel.get().isDeleted()) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(ErrorCodes.INVALID_ORDER_ID.code());
            return;
        }

        OrderModel order = orderModel.get();
        detailModel.setOldOrderStatus(order.getOrderStatus());
        if (order.getOrderStatus().equals(detailModel.getNewOrderStatus())) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(ErrorCodes.INVALID_ORDER_STATUS_CHANGE.code());
            return;
        }
        if (SellSignal.ECOMMERCE_WEB.name().equals(order.getSellSignal()) && OrderStatus.CHANGE_TO_RETAIL.toString().equals(detailModel.getNewOrderStatus())) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(ErrorCodes.CAN_NOT_CHANGE_TO_RETAIL_STATUS_ECOMMERCE_WEB.code());
            return;
        }

        if (order.isImportOrderProcessing()) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(ErrorCodes.CANNOT_CHANGE_ORDER_PROCESSING.code());
        }

    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }
}
