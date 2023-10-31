package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.ToppingOptionData;
import com.vctek.orderservice.model.ToppingOptionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToppingOptionConverter extends AbstractPopulatingConverter<ToppingOptionModel, ToppingOptionData> {

    @Autowired
    private Populator<ToppingOptionModel, ToppingOptionData> toppingOptionDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(ToppingOptionData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(toppingOptionDataPopulator);
    }
}
