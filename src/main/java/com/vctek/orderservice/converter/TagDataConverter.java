package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.model.TagModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagDataConverter extends AbstractPopulatingConverter<TagModel, TagData> {
    @Autowired
    private Populator<TagModel, TagData> tagDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(TagData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(tagDataPopulator);
    }
}
