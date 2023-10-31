package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.LoyaltyRewardSearchExcelData;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateElasticRequest;
import com.vctek.orderservice.elasticsearch.index.LoyaltyRewardRateElasticIndexRunnable;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.elasticsearch.service.LoyaltyRewardRateSearchService;
import com.vctek.orderservice.facade.LoyaltyRewardRateSearchFacade;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.service.ProductLoyaltyRewardRateService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class LoyaltyRewardRateSearchFacadeImpl implements LoyaltyRewardRateSearchFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyRewardRateSearchFacadeImpl.class);
    private Converter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchConverter;
    private LoyaltyRewardRateSearchService searchService;
    private ProductLoyaltyRewardRateService service;
    private int numberOfThread;
    private int bulkSize;
    private static final int MAX_PAGE_SIZE = 200;
    private static final String MINIMUM_SHOULD_MATCH = "1";
    private static final String PRODUCT_ID = "productId";
    private static final String PRODUCT_STRING_NAME = "productStringName";
    private static final String PRODUCT_SKU = "productSku";
    private static final String PRODUCT_NAME = "productName";
    private static final String WILD_CARD_PATTERN = "*%s*";

    @Autowired
    public LoyaltyRewardRateSearchFacadeImpl(
            @Qualifier("loyaltyRewardRateSearchConverter") Converter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchConverter,
            @Value("${vctek.elasticsearch.index.orders.numberOfThread:5}") int numberOfThread,
            @Value("${vctek.elasticsearch.index.orders.bulkSize:100}") int bulkSize) {
        this.loyaltyRewardRateSearchConverter = loyaltyRewardRateSearchConverter;
        this.numberOfThread = numberOfThread;
        this.bulkSize = bulkSize;
    }

    @Override
    public void index(ProductLoyaltyRewardRateModel model) {
        LOGGER.info("Index: loyaltyRewardRateId: {}", model.getId());
        LoyaltyRewardRateSearchModel searchmodel = loyaltyRewardRateSearchConverter.convert(model);
        searchService.save(searchmodel);
    }

    @Override
    public void fullIndex() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            LoyaltyRewardRateElasticIndexRunnable loyaltyRewardRateElasticIndexRunnable = new LoyaltyRewardRateElasticIndexRunnable(authentication, i, bulkSize, numberOfThread);
            loyaltyRewardRateElasticIndexRunnable.setElasticSearchService(searchService);
            loyaltyRewardRateElasticIndexRunnable.setLoyaltyRewardRateSearchModelConverter(loyaltyRewardRateSearchConverter);
            loyaltyRewardRateElasticIndexRunnable.setService(service);
            executorService.execute(loyaltyRewardRateElasticIndexRunnable);
        }
    }

    @Override
    public Page<LoyaltyRewardRateSearchModel> search(LoyaltyRewardRateElasticRequest request, Pageable pageableRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withPageable(pageableRequest);
        if (request.getCompanyId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("companyId", request.getCompanyId()));
        }
        String product = request.getProduct();
        if (StringUtils.isNotBlank(product)) {
            BoolQueryBuilder shouldMatchProduct = new BoolQueryBuilder();
            try {
                long productId = Long.parseLong(product);
                shouldMatchProduct.should(QueryBuilders.matchQuery(PRODUCT_ID, productId))
                        .should(QueryBuilders.matchPhraseQuery(PRODUCT_SKU, QueryParser.escape(product)))
                        .should(QueryBuilders.wildcardQuery(PRODUCT_SKU, String.format(WILD_CARD_PATTERN, QueryParser.escape(product))))
                        .minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            } catch (NumberFormatException e) {
                shouldMatchProduct.should(QueryBuilders.matchPhraseQuery(PRODUCT_STRING_NAME, QueryParser.escape(product)))
                        .should(QueryBuilders.matchPhraseQuery(PRODUCT_NAME, QueryParser.escape(product)))
                        .should(QueryBuilders.matchPhraseQuery(PRODUCT_SKU, QueryParser.escape(product)))
                        .should(QueryBuilders.wildcardQuery(PRODUCT_SKU, String.format(WILD_CARD_PATTERN, QueryParser.escape(product))))
                        .minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            }
            boolQueryBuilder.must(shouldMatchProduct);
        }
        return searchService.search(nativeSearchQueryBuilder.withQuery(boolQueryBuilder).build());
    }

    @Override
    public LoyaltyRewardSearchExcelData exportExcel(Long companyId) {
        LoyaltyRewardRateElasticRequest request = new LoyaltyRewardRateElasticRequest();
        request.setCompanyId(companyId);
        List<LoyaltyRewardRateSearchModel> modelList = new ArrayList<>();
        Pageable pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.ASC, "id"));
        if (org.apache.commons.lang3.StringUtils.isNotBlank(request.getSortField())) {
            if (Sort.Direction.DESC.toString().equalsIgnoreCase(request.getSortOrder())) {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.DESC, request.getSortField()));
            } else {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.ASC, request.getSortField()));
            }
        }

        while (true) {
            Page<LoyaltyRewardRateSearchModel> orderSearchModels = search(request, pageableRequest);
            if (CollectionUtils.isEmpty(orderSearchModels.getContent())) {
                break;
            }
            modelList.addAll(orderSearchModels.getContent());
            pageableRequest = pageableRequest.next();
        }
        LoyaltyRewardSearchExcelData data = new LoyaltyRewardSearchExcelData();
        data.setDataList(modelList);
        data.setContent(exportExcelLoyaltyRewardSearch(data));
        return data;
    }

    protected byte[] exportExcelLoyaltyRewardSearch(LoyaltyRewardSearchExcelData exportExcelData) {
        ClassPathResource resource = new ClassPathResource("templates/tich_diem_san_pham_template.xls");
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                context.putVar("exportExcelDTO", exportExcelData);
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    @Autowired
    public void setSearchService(LoyaltyRewardRateSearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setService(ProductLoyaltyRewardRateService service) {
        this.service = service;
    }
}
