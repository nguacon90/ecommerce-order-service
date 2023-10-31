package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.couponservice.service.CouponCodeGenerationService;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.CouponData;
import com.vctek.orderservice.dto.request.CouponRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.CouponFacade;
import com.vctek.orderservice.service.CouponService;
import org.apache.commons.collections4.CollectionUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CouponFacadeImpl implements CouponFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponFacadeImpl.class);
    private CouponCodeGenerationService couponCodeGenerationService;
    private CouponService couponService;
    private Populator<CouponRequest, CouponModel> couponModelPopulator;
    private Converter<CouponModel, CouponData> couponConverter;
    private Converter<CouponModel, CouponData> basicCouponConverter;

    @Override
    public List<CouponCodeData> generateCouponCodes(CouponCodeConfiguration configuration) {
        if(configuration.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        List<CouponCodeData> couponCodeList = new ArrayList<>();
        Set<String> codes = couponCodeGenerationService.generateCodes(configuration);
        CouponCodeData couponCode;
        for(String code : codes) {
            couponCode = new CouponCodeData();
            couponCode.setCode(code);
            couponCodeList.add(couponCode);
        }

        return couponCodeList;
    }

    @Override
    public CouponData create(CouponRequest request) {
        CouponModel model = new CouponModel();
        couponModelPopulator.populate(request, model);
        model = couponService.save(model);
        return couponConverter.convert(model);
    }

    @Override
    public List<CouponData> findAllForQualifying(Long companyId, Long sourceRuleId) {
        List<CouponModel> couponModels;
        if(sourceRuleId == null) {
            couponModels = couponService.findAllForQualifyingByCompanyId(companyId);
        } else {
            couponModels = couponService.findAllForQualifyingByCompanyIdOrSourceRule(companyId, sourceRuleId);
        }

        return basicCouponConverter.convertAll(couponModels);

    }

    @Override
    public Page<CouponData> findAllBy(Long companyId, String name, Pageable pageable) {
        Page<CouponModel> couponModelPage = couponService.findAllBy(companyId, name, pageable);
        List<CouponModel> couponModels = couponModelPage.getContent();
        if(CollectionUtils.isNotEmpty(couponModels)) {
            List<CouponData> couponDataList = basicCouponConverter.convertAll(couponModels);
            return new PageImpl<>(couponDataList, couponModelPage.getPageable(), couponModelPage.getTotalElements());
        }
        return new PageImpl<>(new ArrayList<>(), couponModelPage.getPageable(), couponModelPage.getTotalElements());
    }

    @Override
    public CouponData getDetail(Long couponId, Long companyId) {
        CouponModel couponModel = couponService.findById(couponId, companyId);
        if(couponModel != null) {
            return couponConverter.convert(couponModel);
        }

        return new CouponData();
    }

    @Override
    public CouponData update(CouponRequest request) {
        CouponModel couponModel = couponService.findById(request.getId(), request.getCompanyId());
        couponModelPopulator.populate(request, couponModel);
        couponModel = couponService.save(couponModel);
        return couponConverter.convert(couponModel);
    }

    @Override
    public void remove(Long couponId, Long companyId) {
        CouponModel couponModel = couponService.findById(couponId, companyId);
        if(couponModel == null) {
            return;
        }

        if(couponModel.getPromotionSourceRule() != null) {
            ErrorCodes err = ErrorCodes.CAN_NOT_REMOVE_COUPON_USING_FOR_PROMOTION;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        couponService.delete(couponModel);
    }

    @Override
    public byte[] exportExcel(Long companyId) {
        Pageable pageable = PageRequest.of(0, 300, Sort.Direction.DESC, "id");
        List<CouponData> couponDataList = new ArrayList<>();
        while (true) {
            Page<CouponModel> pageResult = couponService.findAllByCompanyId(companyId, pageable);
            List<CouponModel> couponModels = pageResult.getContent();
            if (CollectionUtils.isEmpty(couponModels)) {
                break;
            }
            List<CouponData> couponData = basicCouponConverter.convertAll(couponModels);
            couponDataList.addAll(couponData);
            pageable = pageable.next();
        }
        if(CollectionUtils.isEmpty(couponDataList)) {
            return new byte[0];
        }
        ClassPathResource resource = new ClassPathResource("templates/coupon_template.xlsx");
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                context.putVar("couponList", couponDataList);
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    @Autowired
    public void setCouponCodeGenerationService(CouponCodeGenerationService couponCodeGenerationService) {
        this.couponCodeGenerationService = couponCodeGenerationService;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setCouponModelPopulator(Populator<CouponRequest, CouponModel> couponModelPopulator) {
        this.couponModelPopulator = couponModelPopulator;
    }

    @Autowired
    @Qualifier("couponConverter")
    public void setCouponConverter(Converter<CouponModel, CouponData> couponConverter) {
        this.couponConverter = couponConverter;
    }

    @Autowired
    @Qualifier("basicCouponConverter")
    public void setBasicCouponConverter(Converter<CouponModel, CouponData> basicCouponConverter) {
        this.basicCouponConverter = basicCouponConverter;
    }
}
