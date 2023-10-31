package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.TagData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagFacade {
    TagData createOrUpdate(TagData request);

    Page<TagData> findAllBy(TagData tagData, Pageable pageable);
}
