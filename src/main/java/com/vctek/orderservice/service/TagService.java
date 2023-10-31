package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.TagModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {
    TagModel findByIdAndCompanyId(Long id, Long companyId);

    TagModel save(TagModel tagModel);

    Page<TagModel> findAllBy(TagData tagData, Pageable pageable);

    List<TagModel> findByCompanyIdAndName(long companyId, String name);

    List<TagModel> findAllByOrder(OrderModel orderModel);
}
