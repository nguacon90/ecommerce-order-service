package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderNoteData;
import com.vctek.orderservice.model.OrderNoteModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderNoteConverter extends AbstractPopulatingConverter<OrderNoteModel, OrderNoteData> {

    @Autowired
    private Populator<OrderNoteModel, OrderNoteData> orderNoteDataPopulator;

    @Override
    public void setTargetClass() {
            super.setTargetClass(OrderNoteData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(orderNoteDataPopulator);
    }
}
