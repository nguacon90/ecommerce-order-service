package com.vctek.orderservice.converter.coupon;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.CouponData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("basicCouponConverter")
public class BasicCouponConverter extends AbstractPopulatingConverter<CouponModel, CouponData> {

    @Autowired
    @Qualifier("basicCouponDataPopulator")
    private Populator<CouponModel, CouponData> basicCouponDataPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(CouponData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(basicCouponDataPopulator);
    }
}
