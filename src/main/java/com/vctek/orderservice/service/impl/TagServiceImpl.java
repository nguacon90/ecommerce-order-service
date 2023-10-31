package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.repository.TagRepository;
import com.vctek.orderservice.service.TagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    private TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public TagModel findByIdAndCompanyId(Long id, Long companyId) {
        return tagRepository.findByIdAndCompanyId(id, companyId);
    }

    @Override
    public TagModel save(TagModel tagModel) {
        return tagRepository.save(tagModel);
    }

    @Override
    public Page<TagModel> findAllBy(TagData tagData, Pageable pageable) {
        if (StringUtils.isBlank(tagData.getName())) {
            return tagRepository.findAllByCompanyId(tagData.getCompanyId(), pageable);
        }
        return tagRepository.findAllByNameContainingAndCompanyId(tagData.getName(), tagData.getCompanyId(), pageable);
    }

    @Override
    public List<TagModel> findByCompanyIdAndName(long companyId, String name) {
        return tagRepository.findByCompanyIdAndName(companyId, name);
    }

    @Override
    public List<TagModel> findAllByOrder(OrderModel orderModel) {
        return tagRepository.findAllByOrder(orderModel.getId());
    }
}
