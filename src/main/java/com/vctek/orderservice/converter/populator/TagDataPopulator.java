package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.model.TagModel;
import org.springframework.stereotype.Component;

@Component
public class TagDataPopulator implements Populator<TagModel, TagData> {

    @Override
    public void populate(TagModel tagModel, TagData tagData) {
        tagData.setId(tagModel.getId());
        tagData.setName(tagModel.getName());
    }
}
