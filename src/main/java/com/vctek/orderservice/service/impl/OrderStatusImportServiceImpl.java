package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.order.OrderProcessData;
import com.vctek.orderservice.dto.CommerceChangeOrderStatusParameter;
import com.vctek.orderservice.dto.request.OrderStatusImportSearchRequest;
import com.vctek.orderservice.event.OrderStatusImportEvent;
import com.vctek.orderservice.kafka.producer.OrderProcessProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.repository.OrderStatusImportRepository;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.OrderStatusImportDetailService;
import com.vctek.orderservice.service.OrderStatusImportService;
import com.vctek.orderservice.strategy.CommerceChangeOrderStatusStrategy;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderStatusImport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderStatusImportServiceImpl implements OrderStatusImportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusImportServiceImpl.class);
    private OrderStatusImportRepository repository;
    private EntityManager entityManager;
    private OrderService orderService;
    private CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy;
    private OrderProcessProducerService producerService;
    private OrderStatusImportDetailService detailService;
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public OrderStatusImportModel save(OrderStatusImportModel model) {
        OrderStatusImportModel savedModel = repository.save(model);
        updateLockOrders(model);
        OrderStatusImportEvent event = new OrderStatusImportEvent(savedModel);
        applicationEventPublisher.publishEvent(event);
        return savedModel;
    }

    private void updateLockOrders(OrderStatusImportModel model) {
        List<String> orderCodes = model.getOrderStatusImportDetailModels().stream()
                .filter(i -> !OrderStatusImport.ERROR.toString().equals(i.getStatus()))
                .map(i -> i.getOrderCode()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(orderCodes)) {
            orderService.updateLockOrders(model.getCompanyId(), orderCodes, true);
        }
    }

    @Override
    public void handleSendKafkaChangeOrderStatus(OrderStatusImportModel model) {
        List<OrderStatusImportDetailModel> orderStatusImportDetailModels = model.getOrderStatusImportDetailModels().stream()
                .filter(i -> !OrderStatusImport.ERROR.toString().equals(i.getStatus())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(orderStatusImportDetailModels)) {
            Long importOrderStatusId = model.getId();
            Long companyId = model.getCompanyId();
            int size = orderStatusImportDetailModels.size();
            for(int i=0; i < size; i++) {
                OrderStatusImportDetailModel detailModel = orderStatusImportDetailModels.get(i);
                OrderProcessData data = new OrderProcessData();
                data.setImportOrderStatusId(importOrderStatusId);
                data.setImportOrderStatusDetailId(detailModel.getId());
                data.setCompanyId(companyId);
                data.setOrderCode(detailModel.getOrderCode());
                if(i == size - 1) {
                    data.setLastMessage(true);
                }
                producerService.sendOrderStatusImportKafka(data);
            }
        }
    }

    @Override
    public OrderStatusImportModel findByIdAndCompanyId(Long id, Long companyId) {
        return repository.findByIdAndCompanyId(id, companyId);
    }

    @Override
    public Page<OrderStatusImportModel> search(OrderStatusImportSearchRequest request, Pageable pageable) {
        TypedQuery<OrderStatusImportModel> query = getOrderStatusImportModelTypedQuery(request);
        int totalRows = query.getResultList().size();

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<OrderStatusImportModel> models = query.getResultList();
        return new PageImpl<>(models, pageable, totalRows);
    }

    private TypedQuery<OrderStatusImportModel> getOrderStatusImportModelTypedQuery(OrderStatusImportSearchRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderStatusImportModel> cq = cb.createQuery(OrderStatusImportModel.class);
        Root<OrderStatusImportModel> root = cq.from(OrderStatusImportModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("companyId"), request.getCompanyId()));
        if (StringUtils.isNotBlank(request.getOrderCode())) {
            Join<OrderStatusImportModel, OrderStatusImportDetailModel> modelJoin = root.join("orderStatusImportDetailModels");
            predicates.add(cb.equal(modelJoin.get("orderCode"), request.getOrderCode()));
        }

        if (CollectionUtils.isNotEmpty(request.getStatus())) {
            predicates.add(cb.or(root.get("status").in(request.getStatus())));
        }

        if (request.getFromCreatedDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), DateUtil.getDateWithoutTime(request.getFromCreatedDate())));
        }

        if (request.getToCreatedDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), DateUtil.getEndDay(request.getToCreatedDate())));
        }

        cq.orderBy(cb.desc(root.get("id")));
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(cq);
    }

    @Override
    public void changeStatusMultipleOrder(OrderProcessData dto) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(dto.getOrderCode(), dto.getCompanyId());
        OrderStatusImportDetailModel detailModel = detailService.findByIdAndCompanyId(dto.getImportOrderStatusDetailId(), dto.getCompanyId());
        if(detailModel == null) {
            LOGGER.error("Not found import detail model: id: {}", dto.getImportOrderStatusDetailId());
            return;
        }
        OrderStatus newStatus = OrderStatus.findByCode(detailModel.getNewOrderStatus());
        OrderStatusImportModel model = detailModel.getOrderStatusImportModel();
        detailModel.setNewOrderStatus(model.getOrderStatus());

        try {
            detailModel.setStatus(OrderStatusImport.PROCESSING.toString());
            detailService.save(detailModel);
            OrderStatus oldStatus = OrderStatus.findByCode(orderModel.getOrderStatus());
            CommerceChangeOrderStatusParameter parameter = new CommerceChangeOrderStatusParameter(orderModel, oldStatus, newStatus);
            parameter.setImportDetailId(detailModel.getId());
            commerceChangeOrderStatusStrategy.importChangeStatusOrder(parameter, model.getCreatedBy());
        } catch (ServiceException e) {
            detailModel.setStatus(OrderStatusImport.ERROR.toString());
            detailModel.setNote(e.getCode());
            detailService.save(detailModel);
            updateOrderStatusImportAndUnlockOrder(model, dto, orderModel);
        }
    }

    private void updateOrderStatusImportAndUnlockOrder(OrderStatusImportModel model, OrderProcessData dto, OrderModel orderModel) {
        if (orderModel != null) {
            detailService.updateLockOrder(orderModel, false);
        }
        LOGGER.debug("============== CANNOT UPDATE UNLOCK ORDER MODEL CODE: {}", dto.getOrderCode());
        if(!dto.isLastMessage()) {
            return;
        }

        detailService.updateStatusCompletedOrderStatusImportModel(model);
    }

    @Autowired
    public void setRepository(OrderStatusImportRepository repository) {
        this.repository = repository;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setCommerceChangeOrderStatusStrategy(CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy) {
        this.commerceChangeOrderStatusStrategy = commerceChangeOrderStatusStrategy;
    }

    @Autowired
    public void setProducerService(OrderProcessProducerService producerService) {
        this.producerService = producerService;
    }

    @Autowired
    public void setDetailService(OrderStatusImportDetailService detailService) {
        this.detailService = detailService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
