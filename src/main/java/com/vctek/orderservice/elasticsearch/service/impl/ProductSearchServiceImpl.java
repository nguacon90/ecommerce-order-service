package com.vctek.orderservice.elasticsearch.service.impl;

import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.repository.ProductSearchRepository;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {
    public static final String MINIMUM_SHOULD_MATCH = "1";
    public static final String PRODUCT_ID = "id";
    public static final String PRODUCT_SKU = "sku";
    public static final String PRODUCT_BARCODE = "barcode";
    public static final String ALLOW_REWARD = "allowReward";
    private ProductSearchRepository productSearchRepository;

    @Override
    public List<ProductSearchModel> findAllByCompanyId(ProductSearchRequest searchRequest) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withPageable(PageRequest.of(0, searchRequest.getPageSize()));
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        boolQueryBuilder.must(QueryBuilders.matchQuery("companyId", searchRequest.getCompanyId()));
        BoolQueryBuilder matchSkuOrBarcodeBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("deleted", false));
        if (StringUtils.isNotBlank(searchRequest.getSku())) {
            String[] skuList = CommonUtils.splitByComma(searchRequest.getSku());
            for (String sku : skuList) {
                matchSkuOrBarcodeBuilder.should(QueryBuilders.matchQuery(PRODUCT_SKU, sku));
                if(searchRequest.isSearchBarcode()) {
                    matchSkuOrBarcodeBuilder.should(QueryBuilders.matchQuery(PRODUCT_BARCODE, sku));
                }
            }

            matchSkuOrBarcodeBuilder.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(matchSkuOrBarcodeBuilder);
        }

        if (StringUtils.isNotBlank(searchRequest.getIds())) {
            String[] idList = CommonUtils.splitByComma(searchRequest.getIds());
            BoolQueryBuilder matchProductIds = new BoolQueryBuilder();
            for (String id : idList) {
                matchProductIds.should(QueryBuilders.matchQuery(PRODUCT_ID, Long.parseLong(id)));
            }

            matchProductIds.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(matchProductIds);
        }

        if(CollectionUtils.isNotEmpty(searchRequest.getProductIds())) {
            BoolQueryBuilder matchProductIds = new BoolQueryBuilder();
            for (Long id : searchRequest.getProductIds()) {
                matchProductIds.should(QueryBuilders.matchQuery(PRODUCT_ID, id));
            }

            matchProductIds.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(matchProductIds);
        }

        if (searchRequest.isAllowReward() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(ALLOW_REWARD, searchRequest.isAllowReward()));
        }

        Page<ProductSearchModel> searchResult = productSearchRepository.search(nativeSearchQueryBuilder.withQuery(boolQueryBuilder).build());
        List<ProductSearchModel> products = searchResult.getContent();
        return products;
    }

    @Override
    public ProductSearchModel findByExternalIdAndCompanyId(Long externalId, Long companyId) {
        return productSearchRepository.findByExternalIdAndCompanyId(externalId, companyId);
    }

    @Override
    public ProductSearchModel findByIdAndCompanyId(Long id, Long companyId) {
        return productSearchRepository.findByIdAndCompanyId(id, companyId);
    }

    @Override
    public List<ProductSearchModel> findAllByIdIn(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of(0, ids.size());
        Page<ProductSearchModel> page = productSearchRepository.findAllByIdIn(ids, pageable);
        return page.getContent();
    }

    @Override
    public ProductSearchModel findById(Long productId) {
        Optional<ProductSearchModel> optional = productSearchRepository.findById(productId);
        return optional.isPresent() ? optional.get() : null;
    }

    @Autowired
    public void setProductSearchRepository(ProductSearchRepository productSearchRepository) {
        this.productSearchRepository = productSearchRepository;
    }
}
