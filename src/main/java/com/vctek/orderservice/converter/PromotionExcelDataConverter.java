package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.dto.excel.PromotionExcelData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromotionExcelDataConverter extends AbstractPopulatingConverter<PromotionSourceRuleDTO, PromotionExcelData> {
    @Autowired
    private Populator<PromotionSourceRuleDTO, PromotionExcelData> promotionExcelDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(PromotionExcelData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(promotionExcelDataPopulator);
    }
}
