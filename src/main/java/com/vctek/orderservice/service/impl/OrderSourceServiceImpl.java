package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.repository.OrderSourceRepository;
import com.vctek.orderservice.service.OrderSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderSourceServiceImpl implements OrderSourceService {
    private OrderSourceRepository orderSourceRepository;

    @Autowired
    public OrderSourceServiceImpl(OrderSourceRepository orderSourceRepository) {
        this.orderSourceRepository = orderSourceRepository;
    }


    @Override
    public OrderSourceModel save(OrderSourceModel orderSourceModel) {
        return orderSourceRepository.save(orderSourceModel);
    }

    @Override
    public OrderSourceModel findByIdAndCompanyId(Long orderSourceId, Long companyId) {
        Optional<OrderSourceModel> orderSourceModel = orderSourceRepository.findByIdAndCompanyId(orderSourceId, companyId);
        return orderSourceModel.isPresent() ? orderSourceModel.get() : null;
    }

    @Override
    public OrderSourceModel findById(Long orderSourceId) {
        Optional<OrderSourceModel> orderSourceModel = orderSourceRepository.findById(orderSourceId);
        return orderSourceModel.isPresent() ? orderSourceModel.get() : null;
    }

    @Override
    public List<OrderSourceModel> findAllByCompanyId(Long companyId) {
        return orderSourceRepository.findAllByCompanyIdOrderByOrderAsc(companyId);
    }

    @Override
    public List<OrderSourceModel> rearrangeOrder(List<OrderSourceModel> orderSourceModels) {
        return orderSourceRepository.saveAll(orderSourceModels);
    }

    @Override
    public List<OrderSourceModel> findByIdIn(List<Long> ids) {
        return orderSourceRepository.findAllByIdIn(ids);
    }
}
