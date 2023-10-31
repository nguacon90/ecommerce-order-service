package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.OrderDiscountSettingMapper;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.repository.OrderSettingDiscountRepository;
import com.vctek.orderservice.repository.dao.OrderSettingDiscountDAO;
import com.vctek.orderservice.service.OrderSettingDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderSettingDiscountServiceImpl implements OrderSettingDiscountService {
    private OrderSettingDiscountRepository repository;
    private OrderSettingDiscountDAO orderSettingDiscountDAO;

    @Override
    public OrderSettingDiscountModel save(OrderSettingDiscountModel model) {
        return repository.save(model);
    }

    @Override
    public List<OrderSettingDiscountModel> saveAll(List<OrderSettingDiscountModel> models) {
        return repository.saveAll(models);
    }

    @Override
    public List<OrderSettingDiscountModel> findAllByCompanyIdAndProductIdAndDeleted(Long companyId, List<Long> productIds) {
        return repository.findAllByCompanyIdAndProductIdInAndAndDeleted(companyId, productIds, false);
    }

    @Override
    public List<OrderSettingDiscountModel> findAllByCompanyIdAndCategoryCodeAndDeleted(Long companyId, List<String> categoryCodes) {
        return repository.findAllByCompanyIdAndCategoryCodeInAndDeleted(companyId, categoryCodes, false);
    }

    @Override
    public OrderSettingDiscountModel findOneByIdAndCompanyId(Long companyId, Long id) {
        return repository.findByCompanyIdAndId(companyId, id);
    }

    @Override
    public OrderSettingDiscountModel findByCompanyIdAndProductIdAndDeleted(Long companyId, Long productId) {
        return repository.findByCompanyIdAndProductIdAndDeleted(companyId, productId, false);
    }

    @Override
    public Page<OrderSettingDiscountModel> findAllProductSetting(Long companyId, String product, Pageable pageRequest) {
        return orderSettingDiscountDAO.findAllProductSetting(companyId, product, pageRequest);
    }

    @Override
    public List<OrderSettingDiscountModel> findAllCatgorySetting(Long companyId) {
        return repository.findAllByCompanyIdAndCategoryCodeIsNotNullAndDeleted(companyId, false);
    }

    @Override
    public List<OrderDiscountSettingMapper> findAllByProductId(Long companyId, Pageable pageable) {
        return orderSettingDiscountDAO.findAllBy(companyId, pageable);
    }

    @Autowired
    public void setRepository(OrderSettingDiscountRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setOrderSettingDiscountDAO(OrderSettingDiscountDAO orderSettingDiscountDAO) {
        this.orderSettingDiscountDAO = orderSettingDiscountDAO;
    }
}