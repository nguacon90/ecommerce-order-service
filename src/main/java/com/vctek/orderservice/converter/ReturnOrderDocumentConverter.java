package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ReturnOrderDocumentConverter extends AbstractPopulatingConverter<ReturnOrderModel, ReturnOrderDocument> {

    @Autowired
    @Qualifier("returnOrderDocumentPopulator")
    private Populator<ReturnOrderModel, ReturnOrderDocument> returnOrderDocumentPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(ReturnOrderDocument.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(returnOrderDocumentPopulator);
    }
}
