package com.vctek.orderservice.elasticsearch.service;

import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;

import java.util.List;

public interface ProductSearchService {

    List<ProductSearchModel> findAllByCompanyId(ProductSearchRequest searchRequest);

    ProductSearchModel findByExternalIdAndCompanyId(Long externalId, Long companyId);

    ProductSearchModel findByIdAndCompanyId(Long id, Long companyId);

    List<ProductSearchModel> findAllByIdIn(List<Long> ids);

    ProductSearchModel findById(Long productId);
}
