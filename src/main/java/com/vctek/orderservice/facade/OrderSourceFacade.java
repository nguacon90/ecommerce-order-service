package com.vctek.orderservice.facade;


import com.vctek.orderservice.dto.OrderSourceData;
import com.vctek.orderservice.dto.request.OrderSourceRequest;

import java.util.List;

public interface OrderSourceFacade {
    OrderSourceData create(OrderSourceRequest orderSourceRequest);

    OrderSourceData update(OrderSourceRequest orderSourceRequest);

    OrderSourceData findByIdAndCompanyId(Long orderSourceId, Long companyId);

    List<OrderSourceData> findAllByCompanyId(Long companyId);

    List<OrderSourceData> rearrangeOrder(List<OrderSourceRequest> requests);
}
