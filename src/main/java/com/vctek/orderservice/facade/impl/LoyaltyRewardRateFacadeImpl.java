package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.AllLoyaltyRewardRateData;
import com.vctek.orderservice.dto.CategoryLoyaltyRewardRateData;
import com.vctek.orderservice.dto.DefaultLoyaltyRewardRateData;
import com.vctek.orderservice.dto.ProductLoyaltyRewardRateData;
import com.vctek.orderservice.dto.excel.ProductLoyaltyRewardRateDTO;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.excel.ProductLoyaltyRewardRateExcelFileReader;
import com.vctek.orderservice.excel.RowMapperErrorCodes;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.LoyaltyRewardRateFacade;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.service.CategoryLoyaltyRewardRateService;
import com.vctek.orderservice.service.DefaultLoyaltyRewardRateService;
import com.vctek.orderservice.service.ProductLoyaltyRewardRateService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoyaltyRewardRateFacadeImpl implements LoyaltyRewardRateFacade {
    private DefaultLoyaltyRewardRateService defaultLoyaltyRewardRateService;
    private ProductLoyaltyRewardRateService productLoyaltyRewardRateService;
    private CategoryLoyaltyRewardRateService categoryLoyaltyRewardRateService;
    private Converter<DefaultLoyaltyRewardRateModel, DefaultLoyaltyRewardRateData> defaultLoyaltyRewardRateConverter;
    private Converter<ProductLoyaltyRewardRateModel, ProductLoyaltyRewardRateData> productLoyaltyRewardRateConverter;
    private Converter<CategoryLoyaltyRewardRateModel, CategoryLoyaltyRewardRateData> categoryLoyaltyRewardRateConverter;
    private ProductLoyaltyRewardRateExcelFileReader productLoyaltyRewardRateExcelFileReader;
    private ProductService productService;
    private int defaultPageSize = 100;
    private static final String SKU = "SKU";
    private static final String PRODUCT_ID = "PRODUCT_ID";

    @Override
    public AllLoyaltyRewardRateData findBy(Long companyId) {
        AllLoyaltyRewardRateData data = new AllLoyaltyRewardRateData();
        data.setCompanyId(companyId);
        DefaultLoyaltyRewardRateModel defaultModel = defaultLoyaltyRewardRateService.findByCompanyId(companyId);
        if (defaultModel != null) {
            data.setDefaultLoyaltyRewardRate(defaultLoyaltyRewardRateConverter.convert(defaultModel));
        }

        List<CategoryLoyaltyRewardRateModel> categoryLoyaltyRewardRateModels = categoryLoyaltyRewardRateService.findAllByCompanyId(companyId);
        if (CollectionUtils.isNotEmpty(categoryLoyaltyRewardRateModels)) {
            data.setCategoryRateList(categoryLoyaltyRewardRateConverter.convertAll(categoryLoyaltyRewardRateModels));
        }
        return data;
    }

    @Override
    public DefaultLoyaltyRewardRateData createOrUpdateDefault(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest) {
        DefaultLoyaltyRewardRateModel defaultModel = defaultLoyaltyRewardRateService.createOrUpdate(loyaltyRewardRateDetailRequest);
        return defaultLoyaltyRewardRateConverter.convert(defaultModel);
    }

    @Override
    public List<ProductLoyaltyRewardRateData> createOrUpdateProduct(LoyaltyRewardRateRequest loyaltyRewardRateRequest) {
        List<ProductLoyaltyRewardRateModel> models = productLoyaltyRewardRateService.createOrUpdate(loyaltyRewardRateRequest);
        return productLoyaltyRewardRateConverter.convertAll(models);
    }

    @Override
    public List<CategoryLoyaltyRewardRateData> createOrUpdateCategory(LoyaltyRewardRateRequest loyaltyRewardRateRequest) {
        List<CategoryLoyaltyRewardRateModel> models = categoryLoyaltyRewardRateService.createOrUpdate(loyaltyRewardRateRequest);
        return categoryLoyaltyRewardRateConverter.convertAll(models);
    }

    @Override
    public void deleteProduct(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest) {
        productLoyaltyRewardRateService.delete(loyaltyRewardRateDetailRequest);

    }

    @Override
    public void deleteCategory(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest) {
        categoryLoyaltyRewardRateService.delete(loyaltyRewardRateDetailRequest);
    }

    @Override
    public ProductLoyaltyRewardRateDTO importExcelProduct(Long companyId, MultipartFile multipartFile) {
        List<ProductLoyaltyRewardRateDTO> productLoyaltyRewardRate = productLoyaltyRewardRateExcelFileReader.read(multipartFile);
        if (CollectionUtils.isEmpty(productLoyaltyRewardRate)) {
            ErrorCodes err = ErrorCodes.EMPTY_IMPORT_PRODUCT_LOYALTY_REWARD_RATE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        List<ProductLoyaltyRewardRateDTO> loyaltyRewardRateDTOS = productLoyaltyRewardRate.stream().filter(
                item -> StringUtils.isNotBlank(item.getProductId()) || StringUtils.isNotBlank(item.getProductSku()))
                .collect(Collectors.toList());

        Map<String, ProductSearchData> searchDataMapIds = new HashMap<>();
        Map<String, ProductSearchData> searchDataMapSkus = new HashMap<>();
        Map<Long, Double> rewardRateMap = new HashMap<>();

        if(CollectionUtils.isNotEmpty(loyaltyRewardRateDTOS)) {
            Set<String> productSkus = loyaltyRewardRateDTOS.stream().filter(item -> StringUtils.isNotBlank(item.getProductSku()))
                    .map(item -> item.getProductSku()).collect(Collectors.toSet());
            List<String> productIds = loyaltyRewardRateDTOS.stream().filter(item -> StringUtils.isNotBlank(item.getProductId()))
                    .map(item -> item.getProductId()).collect(Collectors.toList());

            List<ProductSearchData> productDataByIds = findProduct(productIds, companyId, PRODUCT_ID);
            List<ProductSearchData> productDataBySkus = findProduct(productSkus, companyId, SKU);

            if(CollectionUtils.isNotEmpty(productDataByIds)) {
                searchDataMapIds = productDataByIds.stream().collect(Collectors.toMap(item -> item.getId().toString(), item -> item, (item1, item2) -> item2));
            }

            if(CollectionUtils.isNotEmpty(productDataBySkus)) {
                searchDataMapSkus = productDataBySkus.stream().collect(Collectors.toMap(item -> item.getSku(), item -> item, (item1, item2) -> item2));
            }
            List<Long> productIdList = productIds.stream().filter(p -> {
                try {
                    Long.valueOf(p);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }).map(p -> Long.valueOf(p)).collect(Collectors.toList());
            List<ProductLoyaltyRewardRateModel> productLoyaltyRewardRateModels = productLoyaltyRewardRateService.findAllByCompanyIdAndProductIds(companyId, productIdList);
            if (CollectionUtils.isNotEmpty(productLoyaltyRewardRateModels)) {
                rewardRateMap = productLoyaltyRewardRateModels.stream().collect(Collectors.toMap(item -> item.getProductId(), item -> item.getRewardRate(),(item1, item2) -> item2));
            }
        }

        return populateProductLoyaltyRewardRate(productLoyaltyRewardRate, searchDataMapIds, searchDataMapSkus, rewardRateMap);
    }


    private List<ProductSearchData> findProduct(Collection<String> searchPrams, Long companyId, String field) {
        List<ProductSearchData> dataList = new ArrayList<>();
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCompanyId(companyId);
        searchRequest.setUnpaged(true);
        searchRequest.setPageSize(defaultPageSize * 2);
        int totalItems = searchPrams.size();
        int mode = totalItems % defaultPageSize;
        int totalPage = totalItems / defaultPageSize;
        if (mode != 0) totalPage++;
        int currentPage = 0;
        List<String> items = new ArrayList<>();
        for (String param : searchPrams) {
            items.add(param);
            if (items.size() == defaultPageSize) {
                currentPage++;
                StringJoiner joiner = new StringJoiner(CommonUtils.COMMA);
                items.stream().forEach(item -> joiner.add(item));
                populateSearchField(searchRequest, field, joiner.toString());
                List<ProductSearchData> productDataList = productService.search(searchRequest);
                if(CollectionUtils.isNotEmpty(productDataList)) {
                    dataList.addAll(productDataList);
                }
                items = new ArrayList<>();
            }
        }

        if (currentPage < totalPage && CollectionUtils.isNotEmpty(items)) {
            StringJoiner joiner = new StringJoiner(CommonUtils.COMMA);
            items.stream().forEach(item -> joiner.add(item));
            populateSearchField(searchRequest, field, joiner.toString());
            List<ProductSearchData> productDataList = productService.search(searchRequest);
            if(CollectionUtils.isNotEmpty(productDataList)) {
                dataList.addAll(productDataList);
            }
        }

        return dataList;
    }

    private void populateSearchField(ProductSearchRequest searchRequest, String field, String value) {
        if (SKU.equals(field)) {
            searchRequest.setSku(value);
        } else {
            searchRequest.setIds(value);
        }
    }

    private ProductLoyaltyRewardRateDTO populateProductLoyaltyRewardRate(List<ProductLoyaltyRewardRateDTO> loyaltyRewardRateDTOS,
                                                  Map<String, ProductSearchData> productMapIds,
                                                  Map<String, ProductSearchData> productMapSkus,
                                                  Map<Long, Double> rewardRateMap) {
        List<ProductLoyaltyRewardRateDTO> rewardRateDTOS = new ArrayList<>();
        List<ProductLoyaltyRewardRateDTO> errors = new ArrayList<>();

        for (ProductLoyaltyRewardRateDTO item : loyaltyRewardRateDTOS) {
            ProductLoyaltyRewardRateDTO rateDTO = new ProductLoyaltyRewardRateDTO();
            if (productMapIds.containsKey(item.getProductId())) {
                ProductSearchData productSearchData = productMapIds.get(item.getProductId());
                rateDTO.setProductId(item.getProductId());
                rateDTO.setProductSku(productSearchData.getSku());
                rateDTO.setProductName(productSearchData.getName());
                rateDTO.setRewardRate(item.getRewardRate());
            } else if (productMapSkus.containsKey(item.getProductSku())) {
                ProductSearchData productSearchData = productMapSkus.get(item.getProductSku());
                rateDTO.setProductId(productSearchData.getId().toString());
                rateDTO.setProductSku(productSearchData.getSku());
                rateDTO.setProductName(productSearchData.getName());
                rateDTO.setRewardRate(item.getRewardRate());
            } else {
                rateDTO.setProductId(item.getProductId());
                rateDTO.setProductSku(item.getProductSku());
                rateDTO.setRowExcel(item.getRowExcel());
                rateDTO.setError(RowMapperErrorCodes.INVALID_PRODUCT_SKU.toString());
                errors.add(rateDTO);
                continue;
            }

            if (StringUtils.isBlank(rateDTO.getError()) && rewardRateMap.containsKey(Long.valueOf(rateDTO.getProductId()))) {
                rateDTO.setOldRewardRate(rewardRateMap.get(Long.valueOf(rateDTO.getProductId())).toString());
            }

            rewardRateDTOS.add(rateDTO);
        }

        ProductLoyaltyRewardRateDTO loyaltyRewardRateDTO = new ProductLoyaltyRewardRateDTO();
        loyaltyRewardRateDTO.setErrors(errors);
        loyaltyRewardRateDTO.setRewardRateData(rewardRateDTOS);
        return loyaltyRewardRateDTO;
    }

    @Autowired
    public void setDefaultLoyaltyRewardRateService(DefaultLoyaltyRewardRateService defaultLoyaltyRewardRateService) {
        this.defaultLoyaltyRewardRateService = defaultLoyaltyRewardRateService;
    }

    @Autowired
    public void setProductLoyaltyRewardRateService(ProductLoyaltyRewardRateService productLoyaltyRewardRateService) {
        this.productLoyaltyRewardRateService = productLoyaltyRewardRateService;
    }

    @Autowired
    public void setCategoryLoyaltyRewardRateService(CategoryLoyaltyRewardRateService categoryLoyaltyRewardRateService) {
        this.categoryLoyaltyRewardRateService = categoryLoyaltyRewardRateService;
    }

    @Autowired
    public void setDefaultLoyaltyRewardRateConverter(Converter<DefaultLoyaltyRewardRateModel, DefaultLoyaltyRewardRateData> defaultLoyaltyRewardRateConverter) {
        this.defaultLoyaltyRewardRateConverter = defaultLoyaltyRewardRateConverter;
    }

    @Autowired
    public void setProductLoyaltyRewardRateConverter(Converter<ProductLoyaltyRewardRateModel, ProductLoyaltyRewardRateData> productLoyaltyRewardRateConverter) {
        this.productLoyaltyRewardRateConverter = productLoyaltyRewardRateConverter;
    }

    @Autowired
    public void setCategoryLoyaltyRewardRateConverter(Converter<CategoryLoyaltyRewardRateModel, CategoryLoyaltyRewardRateData> categoryLoyaltyRewardRateConverter) {
        this.categoryLoyaltyRewardRateConverter = categoryLoyaltyRewardRateConverter;
    }

    @Autowired
    public void setProductLoyaltyRewardRateExcelFileReader(ProductLoyaltyRewardRateExcelFileReader productLoyaltyRewardRateExcelFileReader) {
        this.productLoyaltyRewardRateExcelFileReader = productLoyaltyRewardRateExcelFileReader;
    }

    public ProductService getProductService() {
        return productService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
