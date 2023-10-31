package com.vctek.orderservice.elasticsearch.repository;

import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchModel, Long> {

    List<ProductSearchModel> findBySkuAndCompanyId(String sku, Long companyId);

    ProductSearchModel findByExternalIdAndCompanyId(Long externalId, Long companyId);

    ProductSearchModel findByIdAndCompanyId(Long id, Long companyId);

    Page<ProductSearchModel> findAllByIdIn(List<Long> ids, Pageable pageable);
}
