package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.CheckTotalSellingOfProductRequest;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.CheckProductSellingService;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CheckProductSellingServiceImpl implements CheckProductSellingService {

    private OrderEntryRepository orderEntryRepository;

    public CheckProductSellingServiceImpl(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Override
    public Long countTotalInWholeSaleAndRetail(CheckTotalSellingOfProductRequest request, Long productId) {
        Date fromDate = DateUtils.addDays(new Date(), -request.getFromDate());
        return orderEntryRepository.getListOfProductSellingExcludeOnline(productId, OrderType.ONLINE.toString(), request.getCompanyId(), fromDate);
    }

    @Override
    public Long countTotalInOnline(CheckTotalSellingOfProductRequest request, Long productId) {
        Date fromDate = DateUtils.addDays(new Date(), -request.getFromDate());
        return orderEntryRepository.
                getListOfProductSellingOnline(productId,
                        OrderType.ONLINE.toString(), request.getCompanyId(),
                        fromDate, OrderStatus.COMPLETED.code());
    }
}
