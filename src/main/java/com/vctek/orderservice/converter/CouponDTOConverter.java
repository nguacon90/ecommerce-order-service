package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.CouponDTO;
import com.vctek.orderservice.couponservice.model.CouponModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CouponDTOConverter extends AbstractPopulatingConverter<CouponModel, CouponDTO> {

    @Autowired
    private Populator<CouponModel, CouponDTO> couponDTOPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CouponDTO.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(couponDTOPopulator);
    }
}
