package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.orderservice.dto.UpdateReturnOrderBillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("updateReturnOrderEntriesConverter")
public class UpdateReturnOrderEntriesConverter extends AbstractPopulatingConverter<ReturnOrderBillDTO, UpdateReturnOrderBillDTO> {

    @Autowired
    private Populator<ReturnOrderBillDTO, UpdateReturnOrderBillDTO> updateReturnOrderEntriesPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(UpdateReturnOrderBillDTO.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(updateReturnOrderEntriesPopulator);
    }
}
