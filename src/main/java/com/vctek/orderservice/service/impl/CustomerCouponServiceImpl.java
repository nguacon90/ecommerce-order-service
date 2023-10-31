package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.CustomerCouponDto;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.storefront.UserCouponCodeData;
import com.vctek.orderservice.model.CustomerCouponModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.repository.CouponCodeRepository;
import com.vctek.orderservice.repository.CustomerCouponRepository;
import com.vctek.orderservice.service.CouponService;
import com.vctek.orderservice.service.CustomerCouponService;
import com.vctek.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class CustomerCouponServiceImpl implements CustomerCouponService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerCouponServiceImpl.class);
    private CustomerCouponRepository repository;
    private CouponCodeRepository couponCodeRepository;
    private CouponService couponService;
    private UserService userService;

    public CustomerCouponServiceImpl(CustomerCouponRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void saveCustomerCoupon(CustomerCouponDto dto) {
        CouponModel couponModel = couponService.findById(dto.getCouponId(), dto.getCompanyId());
        if (couponModel == null) {
            LOGGER.debug("======= INVALID_COUPON_ID {}", dto.getCouponId());
            return;
        }
        PromotionSourceRuleModel promotionSourceRuleModel = couponModel.getPromotionSourceRule();
        if (promotionSourceRuleModel == null || (promotionSourceRuleModel.getEndDate() != null && promotionSourceRuleModel.getEndDate().getTime() <= Calendar.getInstance().getTime().getTime())) {
            LOGGER.debug("======= COUPON_ID EXPIRED DATE {}", dto.getCouponId());
            return;
        }
        List<CouponCodeModel> couponCodeModels = couponCodeRepository.findAllByCouponIdAndUnAssignUser(dto.getCouponId());
        if (CollectionUtils.isEmpty(couponCodeModels)) {
            LOGGER.debug("======= Empty coupon {}", dto.getCouponId());
            return;
        }

        CustomerCouponModel model = new CustomerCouponModel();
        model.setUserId(dto.getCustomerId());
        for (CouponCodeModel couponCodeModel : couponCodeModels) {
            CouponCodeModel validatedCoupon = couponService.findValidatedCouponCode(couponCodeModel.getCode(), dto.getCompanyId());
            if (validatedCoupon != null) {
                model.setCouponCodeModel(validatedCoupon);
                repository.save(model);
                return;
            }
        }
    }

    @Override
    public List<UserCouponCodeData> getCouponByUser(Long companyId) {
        List<UserCouponCodeData> dataList = new ArrayList<>();
        Long userId = userService.getCurrentUserId();
        if (userId == null) {
            return dataList;
        }
        List<CustomerCouponModel> couponModels = repository.findAllByUserId(userId);
        if (CollectionUtils.isEmpty(couponModels)) {
            return dataList;
        }
        for (CustomerCouponModel couponModel : couponModels) {
            CouponCodeModel validatedCoupon = couponService.findValidatedCouponCode(couponModel.getCouponCodeModel().getCode(), companyId);
            convertData(dataList, validatedCoupon);
        }
        return dataList;
    }

    private void convertData(List<UserCouponCodeData> dataList, CouponCodeModel validatedCoupon) {
        if (validatedCoupon == null) {
            return;
        }
        UserCouponCodeData data = new UserCouponCodeData();
        data.setCode(validatedCoupon.getCode());
        data.setName(validatedCoupon.getCoupon().getName());
        dataList.add(data);
    }

    @Autowired
    public void setCouponCodeRepository(CouponCodeRepository couponCodeRepository) {
        this.couponCodeRepository = couponCodeRepository;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
