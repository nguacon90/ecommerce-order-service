package com.vctek.orderservice.service;

import com.vctek.orderservice.model.OrderSourceModel;

import java.util.List;

public interface OrderSourceService {

    OrderSourceModel save(OrderSourceModel orderSourceModel);

    OrderSourceModel findByIdAndCompanyId(Long orderSourceId, Long companyId);

    OrderSourceModel findById(Long orderSourceId);

    List<OrderSourceModel> findAllByCompanyId(Long companyId);

    List<OrderSourceModel> rearrangeOrder(List<OrderSourceModel> orderSourceModels);

    List<OrderSourceModel> findByIdIn(List<Long> ids);
}
