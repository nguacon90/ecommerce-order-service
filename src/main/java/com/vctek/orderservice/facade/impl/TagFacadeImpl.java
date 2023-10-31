package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.facade.TagFacade;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.TagService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TagFacadeImpl implements TagFacade {
    private TagService tagService;
    private Converter<TagModel, TagData> tagDataConverter;

    @Override
    public TagData createOrUpdate(TagData request) {
        TagModel tagModel;
        if (request.getId() != null) {
            tagModel = tagService.findByIdAndCompanyId(request.getId(), request.getCompanyId());
        } else {
            tagModel = new TagModel();
            tagModel.setCompanyId(request.getCompanyId());
        }
        tagModel.setName(request.getName());
        TagModel savedModel = tagService.save(tagModel);
        return tagDataConverter.convert(savedModel);
    }

    @Override
    public Page<TagData> findAllBy(TagData tagData, Pageable pageable) {
        Page<TagModel> modelPage = tagService.findAllBy(tagData, pageable);
        if (CollectionUtils.isEmpty(modelPage.getContent())) {
            return new PageImpl<>(new ArrayList<>(), modelPage.getPageable(), 0);
        }

        List<TagData> dataList = tagDataConverter.convertAll(modelPage.getContent());
        return new PageImpl<>(dataList, modelPage.getPageable(), modelPage.getTotalElements());
    }

    @Autowired
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    @Autowired
    public void setTagDataConverter(Converter<TagModel, TagData> tagDataConverter) {
        this.tagDataConverter = tagDataConverter;
    }
}
