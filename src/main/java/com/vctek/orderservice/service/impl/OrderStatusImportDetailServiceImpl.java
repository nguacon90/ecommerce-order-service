package com.vctek.orderservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.dto.health.ServiceName;
import com.vctek.kafka.data.OrderProcessResultData;
import com.vctek.orderservice.dto.IntegrationServiceStatusData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.repository.OrderStatusImportDetailRepository;
import com.vctek.orderservice.repository.OrderStatusImportRepository;
import com.vctek.orderservice.service.InvoiceService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.OrderStatusImportDetailService;
import com.vctek.util.OrderProcessResultStatus;
import com.vctek.util.OrderStatusImport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderStatusImportDetailServiceImpl implements OrderStatusImportDetailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusImportDetailServiceImpl.class);
    private OrderService orderService;
    private OrderStatusImportDetailRepository repository;
    private ObjectMapper objectMapper;
    private OrderStatusImportRepository orderStatusImportRepository;
    private InvoiceService invoiceService;

    @Override
    @Transactional
    public void updateStatusAndUnlockOrder(OrderProcessResultData data) {
        if (data.getImportDetailId() == null) {
            LOGGER.warn("=========== ImportDetailId is NULL");
            return;
        }
        Optional<OrderStatusImportDetailModel> optional = repository.findByIdAndCompanyId(data.getImportDetailId(), data.getCompanyId());
        if (!optional.isPresent()) {
            LOGGER.warn("Cannot exits OrderStatusImportDetailModel id: {}", data.getImportDetailId());
            return;
        }
        OrderStatusImportDetailModel model = optional.get();
        if (OrderProcessResultStatus.ERROR.toString().equals(data.getStatus()) && StringUtils.isNotBlank(data.getErrorCode())) {
            List<String> errorCodeList = new ArrayList<>();
            if (StringUtils.isNotBlank(model.getNote())) {
                String[] codes = model.getNote().split(",");
                errorCodeList.addAll(Arrays.stream(codes).collect(Collectors.toList()));
            }
            errorCodeList.add(data.getErrorCode());
            model.setNote(StringUtils.join(errorCodeList, ","));
        }
        List<IntegrationServiceStatusData> dataList = getIntegrationServiceStatus(model.getIntegrationServiceStatus());
        populateIntegrationServiceStatus(dataList, data);
        boolean unlock = unLockOrderModel(dataList, data);
        if(unlock) {
            model.setStatus(OrderStatusImport.COMPLETED.toString());
        } else if(OrderProcessResultStatus.ERROR.toString().equals(data.getStatus())) {
            model.setStatus(OrderStatusImport.ERROR.toString());
        }

        try {
            model.setIntegrationServiceStatus(objectMapper.writeValueAsString(dataList));
        } catch (IOException e) {
            LOGGER.warn("Cannot write Value As String");
        }
        repository.save(model);
        LOGGER.debug("===================== UPDATE STATUS OrderStatusImportDetailModel");
        updateStatusCompletedOrderStatusImportModel(model);
        mapInvoiceToPaymentTransaction(data);
    }

    private void mapInvoiceToPaymentTransaction(OrderProcessResultData data) {
        if(data.getPaymentInvoiceData() != null) {
            invoiceService.mapInvoiceToPaymentTransaction(data.getPaymentInvoiceData());
        }
    }

    private void updateStatusCompletedOrderStatusImportModel(OrderStatusImportDetailModel detailModel) {
        OrderStatusImportModel model = detailModel.getOrderStatusImportModel();
        if (model == null) {
            return;
        }
        this.updateStatusCompletedOrderStatusImportModel(model);
    }

    @Override
    public List<OrderStatusImportDetailModel> findAllByOrderStatusImportIdAndCompanyIdAndIdIn(Long orderStatusImportId, Long companyId, List<Long> detailId) {
        return repository.findAllByOrderStatusImportIdAndCompanyIdAndIdIn(orderStatusImportId, companyId, detailId);
    }

    @Override
    public List<OrderStatusImportDetailModel> saveAll(List<OrderStatusImportDetailModel> models) {
        return repository.saveAll(models);
    }

    protected boolean unLockOrderModel(List<IntegrationServiceStatusData> dataList, OrderProcessResultData data) {
        Optional<IntegrationServiceStatusData> statusError = dataList.stream().filter(i -> OrderProcessResultStatus.ERROR.toString().equals(i.getStatus())).findFirst();
        OrderModel orderModel = orderService.findByCodeAndCompanyId(data.getOrderCode(), data.getCompanyId());
        if(orderModel == null) {
            return false;
        }

        if (statusError.isPresent()) {
            updateLockOrder(orderModel, true);
            return false;
        }

        if(!ServiceName.LOGISTIC.toString().equals(data.getServiceName())) {
            return !orderModel.isImportOrderProcessing();
        }

        updateLockOrder(orderModel, false);
        return true;
    }

    @Override
    public void updateLockOrder(OrderModel orderModel, boolean lockOrder) {
        if (orderModel.isImportOrderProcessing() != lockOrder) {
            orderModel.setImportOrderProcessing(lockOrder);
            orderService.save(orderModel);
            LOGGER.debug("============== UPDATE LOCK ORDER MODEL CODE: {}, lockOrder: {}", orderModel.getCode(), lockOrder);
            return;
        }
        LOGGER.debug("============== CANNOT UPDATE LOCK ORDER MODEL CODE: {}, oldLockOrder: {}, newLockOrder", orderModel.getCode(), orderModel.isImportOrderProcessing(),  lockOrder);
    }

    private void populateIntegrationServiceStatus(List<IntegrationServiceStatusData> dataList, OrderProcessResultData data) {
        if (CollectionUtils.isEmpty(dataList)) {
            dataList.add(populateBaseObjectData(data));
            return;
        }

        Optional<IntegrationServiceStatusData> optional = dataList.stream()
                .filter(i -> StringUtils.isNotBlank(data.getServiceName())
                && data.getServiceName().equals(i.getName())).findFirst();
        if (!optional.isPresent()) {
            dataList.add(populateBaseObjectData(data));
            return;
        }

        IntegrationServiceStatusData existedBaseObjectData = optional.get();
        existedBaseObjectData.setStatus(data.getStatus());
    }

    private IntegrationServiceStatusData populateBaseObjectData(OrderProcessResultData data) {
        IntegrationServiceStatusData objectData = new IntegrationServiceStatusData();
        objectData.setName(data.getServiceName());
        objectData.setStatus(data.getStatus());
        return objectData;
    }

    private List<IntegrationServiceStatusData> getIntegrationServiceStatus(String integrationServiceStatus) {
        List<IntegrationServiceStatusData> data = new ArrayList<>();
        if (StringUtils.isBlank(integrationServiceStatus)) return data;
        try {
             data = objectMapper.readValue(integrationServiceStatus, new TypeReference<List<IntegrationServiceStatusData>>(){});
        } catch (IOException e) {
            LOGGER.warn("Cannot parse integrationServiceStatus {}", e.getMessage());
        }
        return data;
    }

    @Override
    public OrderStatusImportDetailModel save(OrderStatusImportDetailModel model) {
        return repository.save(model);
    }

    @Override
    public OrderStatusImportDetailModel findByIdAndCompanyId(Long id, Long companyId) {
        Optional<OrderStatusImportDetailModel> optional = repository.findByIdAndCompanyId(id, companyId);
        return optional.isPresent() ? optional.get() : null;
    }

    @Override
    public void updateStatusCompletedOrderStatusImportModel(OrderStatusImportModel model) {
        Optional<OrderStatusImportDetailModel> optionalProcess = repository.findDistinctTopByOrderStatusImportModelAndStatus(model, OrderStatusImport.PROCESSING.toString());
        if (!optionalProcess.isPresent()) {
            model.setStatus(OrderStatusImport.COMPLETED.toString());
            orderStatusImportRepository.save(model);
            LOGGER.debug("============== UPDATE STATUS COMPLETED ID: {}", model.getId());
        }
        LOGGER.debug("============== CANNOT UPDATE STATUS COMPLETED ID: {}", model.getId());
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setRepository(OrderStatusImportDetailRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setOrderStatusImportRepository(OrderStatusImportRepository orderStatusImportRepository) {
        this.orderStatusImportRepository = orderStatusImportRepository;
    }

    @Autowired
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }
}
