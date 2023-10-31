package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CategoryData;
import com.vctek.orderservice.dto.OrderDiscountSettingMapper;
import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.dto.excel.OrderSettingDiscountDTO;
import com.vctek.orderservice.dto.excel.OrderSettingDiscountErrorDTO;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.excel.OrderSettingDiscountExcelFileReader;
import com.vctek.orderservice.excel.RowMapperErrorCodes;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderSettingDiscountFacade;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.service.OrderSettingDiscountService;
import com.vctek.orderservice.service.OrderSettingService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.OrderSettingType;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderSettingDiscountFacadeImpl implements OrderSettingDiscountFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSettingDiscountFacadeImpl.class);
    private OrderSettingDiscountService service;
    private ProductSearchService productSearchService;
    private ProductService productService;
    private OrderSettingService orderSettingService;
    private OrderSettingDiscountExcelFileReader orderSettingDiscountExcelFileReader;
    private Converter<OrderSettingDiscountModel, OrderSettingDiscountData> orderSettingDiscountConverter;
    private Converter<OrderSettingModel, OrderSettingData> orderSettingConverter;

    @Override
    public OrderSettingData createProduct(OrderSettingRequest request) {
        Long companyId = request.getCompanyId();
        OrderSettingModel model = getOrderSettingModel(request);

        List<Long> productIds = request.getSettingDiscountData().stream().map(i -> i.getProductId()).collect(Collectors.toList());
        List<OrderSettingDiscountModel> modelProductSetting = service.findAllByCompanyIdAndProductIdAndDeleted(companyId, productIds);
        List<OrderSettingDiscountModel> modelList = new ArrayList<>();
        for (OrderSettingDiscountData discountData : request.getSettingDiscountData()) {
            OrderSettingDiscountModel productSetting = findOneByProductId(discountData, modelProductSetting);
            productSetting = populateModel(discountData, productSetting);

            productSetting.setSettingModel(model);
            productSetting.setCompanyId(companyId);
            productSetting.setProductId(discountData.getProductId());
            modelList.add(productSetting);
        }

        model.setSettingDiscountModel(modelList);
        OrderSettingModel savedModels = orderSettingService.save(model);

        return orderSettingConverter.convert(savedModels);
    }

    @Override
    public OrderSettingData createOrUpdateCategory(OrderSettingRequest request) {
        Long companyId = request.getCompanyId();
        OrderSettingModel orderSettingModel = getOrderSettingModel(request);

        List<String> categoryCodes = request.getSettingDiscountData().stream().map(i -> i.getCategoryCode()).collect(Collectors.toList());
        List<OrderSettingDiscountModel> settingDiscountOldModels = service.findAllByCompanyIdAndCategoryCodeAndDeleted(companyId, categoryCodes);
        List<OrderSettingDiscountModel> settingDiscountModels = new ArrayList<>();
        for (OrderSettingDiscountData discountData : request.getSettingDiscountData()) {
            OrderSettingDiscountModel model = findOneByCategoryCode(discountData, settingDiscountOldModels);
            model = populateModel(discountData, model);

            model.setSettingModel(orderSettingModel);
            model.setCompanyId(companyId);
            model.setCategoryCode(discountData.getCategoryCode());
            settingDiscountModels.add(model);
        }

        orderSettingModel.setSettingDiscountModel(settingDiscountModels);
        OrderSettingModel savedModels = orderSettingService.save(orderSettingModel);

        return orderSettingConverter.convert(savedModels);
    }

    @Override
    public void deleteProductSetting(Long settingId, Long companyId) {
        OrderSettingModel orderSettingModel = orderSettingService.findByTypeAndCompanyId(
                OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString(), companyId);
        if (orderSettingModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SETTING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderSettingDiscountModel model = service.findOneByIdAndCompanyId(companyId, settingId);
        if (model == null) {
            ErrorCodes err = ErrorCodes.INVALID_SETTING_MAXIMUM_DISCOUNT_PRODUCT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        model.setDeleted(true);
        service.save(model);
    }

    private OrderSettingModel getOrderSettingModel(OrderSettingRequest request) {
        OrderSettingModel model = orderSettingService.findByTypeAndCompanyId(request.getType(), request.getCompanyId());
        if (model == null) model = new OrderSettingModel();
        model.setCompanyId(request.getCompanyId());
        model.setOrderTypes(request.getOrderTypes());
        model.setType(OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString());
        return model;
    }

    private OrderSettingDiscountModel findOneByProductId(OrderSettingDiscountData request, List<OrderSettingDiscountModel> models) {
        Optional<OrderSettingDiscountModel> model = models.stream().filter(i -> i.getProductId().equals(request.getProductId())).findFirst();
        return model.isPresent() ? model.get() : null;
    }

    private OrderSettingDiscountModel populateModel(OrderSettingDiscountData request, OrderSettingDiscountModel model) {
        if (model == null) {
            model = new OrderSettingDiscountModel();
        }

        model.setDiscount(request.getDiscount());
        model.setDiscountType(request.getDiscountType());
        return model;
    }

    private OrderSettingDiscountModel findOneByCategoryCode(OrderSettingDiscountData request, List<OrderSettingDiscountModel> models) {
        Optional<OrderSettingDiscountModel> model = models.stream().filter(i -> i.getCategoryCode().equals(request.getCategoryCode())).findFirst();
        return model.isPresent() ? model.get() : null;
    }

    @Override
    public Page<OrderSettingDiscountData> search(Long companyId, String product, Pageable pageableRequest) {
        Page<OrderSettingDiscountModel> modelPage = service.findAllProductSetting(companyId, product, pageableRequest);
        List<OrderSettingDiscountData> data = orderSettingDiscountConverter.convertAll(modelPage.getContent());
        return new PageImpl<>(data, modelPage.getPageable(), modelPage.getTotalElements());
    }

    @Override
    public OrderSettingData findAllCategory(Long companyId) {
        OrderSettingModel orderSettingModel = orderSettingService.findByTypeAndCompanyId(
                OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString(), companyId);
        if (orderSettingModel == null) return new OrderSettingData();
        return orderSettingConverter.convert(orderSettingModel);
    }

    @Override
    public byte[] exportExcel(Long companyId) {
        Pageable pageable = PageRequest.of(0, 1000);
        List<OrderDiscountSettingMapper> allDiscountList = new ArrayList<>();
        while (true) {
            List<OrderDiscountSettingMapper> discountList = service.findAllByProductId(companyId, pageable);
            if (CollectionUtils.isEmpty(discountList)) {
                break;
            }

            List<Long> productIds = discountList.stream().map(i -> i.getProductId()).collect(Collectors.toList());
            ProductSearchRequest searchRequest = new ProductSearchRequest();
            searchRequest.setCompanyId(companyId);
            searchRequest.setPageSize(productIds.size());
            searchRequest.setIds(StringUtils.join(productIds, ","));

            List<ProductSearchModel> productSearchModels = productSearchService.findAllByCompanyId(searchRequest);
            Map<Long, ProductSearchModel> productSearchModelMap = productSearchModels.stream()
                    .collect(Collectors.toMap(i -> i.getId(), Function.identity()));

            for (OrderDiscountSettingMapper mapper : discountList) {
                ProductSearchModel model = productSearchModelMap.get(mapper.getProductId());
                if (model != null) {
                    mapper.setSku(model.getSku());
                    mapper.setName(model.getName());
                }
            }

            allDiscountList.addAll(discountList);
            pageable = pageable.next();
        }

        ClassPathResource resource = new ClassPathResource("templates/order_discount_setting_template.xlsx");
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                context.putVar("data", allDiscountList);
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    @Override
    public OrderSettingDiscountErrorDTO importExcel(Long companyId, MultipartFile multipartFile) {
        OrderSettingDiscountErrorDTO errorDTO = new OrderSettingDiscountErrorDTO();
        OrderSettingModel orderSettingModel = orderSettingService.findByTypeAndCompanyId(OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString(), companyId);
        if (orderSettingModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SETTING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        List<OrderSettingDiscountDTO> productSettingExcel = orderSettingDiscountExcelFileReader.read(multipartFile);
        if (CollectionUtils.isEmpty(productSettingExcel)) {
            ErrorCodes err = ErrorCodes.EMPTY_IMPORT_PRODUCT_SETTING_DISCOUNT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        List<OrderSettingDiscountDTO> errors = populateProductSettingError(productSettingExcel, companyId);
        if (CollectionUtils.isNotEmpty(errors)) {
            Collections.sort(errors, Comparator.comparing(OrderSettingDiscountDTO::getRowExcel));
            errorDTO.setErrors(errors);
            return errorDTO;
        }
        List<OrderSettingDiscountModel> productSettingModels = populateProductSettingModel(productSettingExcel, orderSettingModel);
        service.saveAll(productSettingModels);
        return errorDTO;
    }

    private List<OrderSettingDiscountModel> populateProductSettingModel(List<OrderSettingDiscountDTO> productSettingExcel, OrderSettingModel orderSettingModel) {
        Long companyId = orderSettingModel.getCompanyId();
        List<OrderSettingDiscountModel> models = new ArrayList<>();
        for (OrderSettingDiscountDTO item : productSettingExcel) {
            OrderSettingDiscountModel model = service.findByCompanyIdAndProductIdAndDeleted(companyId, item.getProductId());
            model = model != null ? model : new OrderSettingDiscountModel();
            model.setCompanyId(companyId);
            model.setSettingModel(orderSettingModel);
            model.setProductId(item.getProductId());
            model.setDiscount(CommonUtils.strToDouble(item.getDiscount()));
            model.setDiscountType(item.getDiscountType());
            models.add(model);
        }
        return models;
    }

    private List<OrderSettingDiscountDTO> populateProductSettingError(List<OrderSettingDiscountDTO> productSettingExcel, Long companyId) {
        List<OrderSettingDiscountDTO> discountDTOS = new ArrayList<>();
        List<String> productSku = productSettingExcel.stream().filter(item -> StringUtils.isNotBlank(item.getProductSku()))
                .map(item -> item.getProductSku()).collect(Collectors.toList());
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCompanyId(companyId);
        searchRequest.setPageSize(productSku.size());
        searchRequest.setSku(StringUtils.join(productSku, ","));

        List<ProductSearchModel> productDataBySkus = productSearchService.findAllByCompanyId(searchRequest);
        Map<String, Long> searchDataMapSkus = new HashMap<>();
        if (CollectionUtils.isNotEmpty(productDataBySkus)) {
            searchDataMapSkus = productDataBySkus.stream().collect(Collectors.toMap(item -> item.getSku(), item -> item.getId(), (item1, item2) -> item2));
        }

        for (OrderSettingDiscountDTO item : productSettingExcel) {
            item.setProductId(searchDataMapSkus.get(item.getProductSku()));
            validateProductExcel(item, discountDTOS, searchDataMapSkus);
            validateOrderDiscountExcel(item, discountDTOS);
            validateOrderDiscountTypeExcel(item, discountDTOS);
        }

        return discountDTOS;
    }

    private void validateOrderDiscountTypeExcel(OrderSettingDiscountDTO item, List<OrderSettingDiscountDTO> discountDTOS) {
        OrderSettingDiscountDTO dto = new OrderSettingDiscountDTO();
        dto.setRowExcel(item.getRowExcel());
        if (StringUtils.isEmpty(item.getDiscountType())) {
            dto.setError(RowMapperErrorCodes.EMPTY_DISCOUNT_TYPE.toString());
            discountDTOS.add(dto);
        } else {
            CurrencyType currencyType = CurrencyType.findCurrencyByCode(item.getDiscountType());
            if (currencyType == null) {
                dto.setError(RowMapperErrorCodes.WRONG_FORMAT.toString());
                discountDTOS.add(dto);
            }
            if (currencyType != null) {
                item.setDiscountType(currencyType.toString());
            }
        }
    }

    private void validateOrderDiscountExcel(OrderSettingDiscountDTO item, List<OrderSettingDiscountDTO> discountDTOS) {
        OrderSettingDiscountDTO dto = new OrderSettingDiscountDTO();
        dto.setRowExcel(item.getRowExcel());
        if (StringUtils.isEmpty(item.getDiscount())) {
            dto.setError(RowMapperErrorCodes.INVALID_DISCOUNT.toString());
            discountDTOS.add(dto);
        } else {
            try {
                double discountValue = CommonUtils.strToDouble(item.getDiscount());
                if (discountValue < 0) {
                    dto.setError(RowMapperErrorCodes.MUST_LAGRE_MORE.toString());
                    discountDTOS.add(dto);
                }
            } catch (NumberFormatException e) {
                dto.setError(RowMapperErrorCodes.WRONG_DISCOUNT_FORMAT.toString());
                discountDTOS.add(dto);
            }
        }
    }

    private void validateProductExcel(OrderSettingDiscountDTO item, List<OrderSettingDiscountDTO> discountDTOS, Map<String, Long> searchDataMapSkus) {
        OrderSettingDiscountDTO dto = new OrderSettingDiscountDTO();
        dto.setRowExcel(item.getRowExcel());
        if (StringUtils.isEmpty(item.getProductSku())) {
            dto.setError(RowMapperErrorCodes.EMPTY_SKU.toString());
            discountDTOS.add(dto);
        } else {
            if (!searchDataMapSkus.containsKey(item.getProductSku())) {
                dto.setError(RowMapperErrorCodes.INVALID_PRODUCT_SKU.toString());
                dto.setProductSku(item.getProductSku());
                discountDTOS.add(dto);
            }
        }
    }

    @Override
    public Map<Long, OrderSettingDiscountData> getDiscountProduct(Long companyId, List<Long> productIds) {
        List<Long> listProductId = productIds.stream().collect(Collectors.toList());
        List<OrderSettingDiscountModel> models = service.findAllByCompanyIdAndProductIdAndDeleted(companyId, listProductId);
        Map<Long, OrderSettingDiscountData> modelMap = new HashMap<>();

        List<OrderSettingDiscountData> dataList = orderSettingDiscountConverter.convertAll(models);

        for (OrderSettingDiscountData data : dataList) {
            int index = listProductId.indexOf(data.getProductId());
            if (index >= 0) {
                modelMap.put(data.getProductId(), data);
                listProductId.remove(index);
            }
        }

        if (CollectionUtils.isNotEmpty(listProductId)) {
            getDiscountProductByCategory(companyId, listProductId, modelMap);
        }

        return modelMap;
    }

    private void getDiscountProductByCategory(Long companyId, List<Long> listProductId, Map<Long, OrderSettingDiscountData> modelMap) {
        for (Long productId : listProductId) {
            List<CategoryData> categoryData = productService.findAllProductCategories(productId);
            if (CollectionUtils.isNotEmpty(categoryData)) {
                List<String> categoryCodes = categoryData.stream().map(i -> i.getCode()).collect(Collectors.toList());
                List<OrderSettingDiscountModel> modelCategory = service.findAllByCompanyIdAndCategoryCodeAndDeleted(companyId, categoryCodes);

                Map<String, OrderSettingDiscountModel> mapDiscountCategory = modelCategory.stream().collect(Collectors.toMap(i -> i.getCategoryCode(), Function.identity()));

                for (CategoryData data : categoryData) {
                    OrderSettingDiscountModel discountModel = mapDiscountCategory.get(data.getCode());
                    if (discountModel != null) {
                        modelMap.put(productId, orderSettingDiscountConverter.convert(discountModel));
                        break;
                    }
                }
            }
        }
    }

    @Autowired
    public void setService(OrderSettingDiscountService service) {
        this.service = service;
    }

    @Autowired
    public void setOrderSettingDiscountConverter(Converter<OrderSettingDiscountModel, OrderSettingDiscountData> orderSettingDiscountConverter) {
        this.orderSettingDiscountConverter = orderSettingDiscountConverter;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setOrderSettingDiscountExcelFileReader(OrderSettingDiscountExcelFileReader orderSettingDiscountExcelFileReader) {
        this.orderSettingDiscountExcelFileReader = orderSettingDiscountExcelFileReader;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setOrderSettingService(OrderSettingService orderSettingService) {
        this.orderSettingService = orderSettingService;
    }

    @Autowired
    public void setOrderSettingConverter(Converter<OrderSettingModel, OrderSettingData> orderSettingConverter) {
        this.orderSettingConverter = orderSettingConverter;
    }
}
