package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.dto.CategoryData;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.excel.PromotionDefinitionData;
import com.vctek.orderservice.dto.excel.PromotionExcelData;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.CommonUtils;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Component
public class PromotionExcelDataPopulator implements Populator<PromotionSourceRuleDTO, PromotionExcelData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionExcelDataPopulator.class);
    private LogisticService logisticService;
    private ObjectMapper objectMapper;
    private ProductSearchService productSearchService;
    private ProductService productService;

    @Override
    public void populate(PromotionSourceRuleDTO source, PromotionExcelData target) {
        target.setCampaignName(source.getCampaignName());
        target.setMessageFired(source.getMessageFired());
        target.setPriority(source.getPriority());
        if(source.getStartDate() != null) {
            target.setStartDateStr(DateUtil.getDateStr(source.getStartDate(), DateUtil.VN_PATTERN));
        }

        if(source.getEndDate() != null) {
            target.setEndDateStr(DateUtil.getDateStr(source.getEndDate(), DateUtil.VN_PATTERN));
        }

        if(source.isActive()) {
            target.setActiveStatus(CommonUtils.ACTIVE_STATUS);
        } else {
            target.setActiveStatus(CommonUtils.INACTIVE_STATUS);
        }

        if(RuleStatus.PUBLISHED.toString().equals(source.getPublishedStatus())) {
            target.setPublishStatusStr(CommonUtils.PUBLISH_STATUS);
        } else {
            target.setPublishStatusStr(CommonUtils.UNPUBLISH_STATUS);
        }

        populateAppliedWarehouses(source, target);
        populateAppliedOrderTypes(source, target);
        populateConditions(source, target);
    }

    private void populateConditions(PromotionSourceRuleDTO source, PromotionExcelData target) {
        List<ConditionDTO> conditions = source.getConditions();
        if(CollectionUtils.isEmpty(conditions)) {
            return;
        }
        PromotionDefinitionData data = new PromotionDefinitionData();
        populateConditionProductAndCategory(data, conditions);
        StringBuilder stringBuilder = new StringBuilder();
        boolean appendBreakLine = false;
        if(CollectionUtils.isNotEmpty(data.getProductIds())) {
            List<ProductSearchModel> models = productSearchService.findAllByIdIn(data.getProductIds().stream().collect(Collectors.toList()));
            stringBuilder.append("Sản phẩm: ");
            StringJoiner joiner = new StringJoiner(com.vctek.util.CommonUtils.COMMA);
            models.forEach(p -> {
                joiner.add(" (" + p.getId() + ") " + p.getName());
            });
            stringBuilder.append(joiner);
            appendBreakLine = true;
        }
        if(CollectionUtils.isNotEmpty(data.getCategoryIds())) {
            if(appendBreakLine) {
                stringBuilder.append("\n");
            }
            List<CategoryData> categoryList = productService.getCategoryByIdIn(data.getCategoryIds(), source.getCompanyId());
            stringBuilder.append("Danh mục: ");
            StringJoiner joiner = new StringJoiner(com.vctek.util.CommonUtils.COMMA);
            categoryList.forEach(c -> {
                joiner.add(" (" + c.getCode() + ") " + c.getName());
            });
            stringBuilder.append(joiner);
        }

        target.setConditionsStr(stringBuilder.toString());
    }

    protected void populateConditionProductAndCategory(PromotionDefinitionData data, List<ConditionDTO> conditions) {

        for (ConditionDTO conditionDTO : conditions) {
            Map<String, ParameterDTO> parameters = conditionDTO.getParameters();
            if(MapUtils.isEmpty(parameters)) {
                continue;
            }
            parameters.forEach((key, param) -> {
                if (ConditionDefinitionParameter.QUALIFYING_PRODUCTS.code().equalsIgnoreCase(key)) {
                    List<Long> ids = objectMapper.convertValue(param.getValue(), new TypeReference<List<Long>>() {
                    });
                    data.getProductIds().addAll(ids);
                } else if (ConditionDefinitionParameter.QUALIFYING_CATEGORIES.code().equalsIgnoreCase(key)) {
                    List<Long> ids = objectMapper.convertValue(param.getValue(), new TypeReference<List<Long>>() {
                    });
                    data.getCategoryIds().addAll(ids);
                }
            });

            if (CollectionUtils.isNotEmpty(conditionDTO.getChildren())) {
                this.populateConditionProductAndCategory(data, conditionDTO.getChildren());
            }
        }
    }

    private void populateAppliedOrderTypes(PromotionSourceRuleDTO source, PromotionExcelData target) {
        List<String> orderTypes = source.getOrderTypes();
        if(CollectionUtils.isEmpty(orderTypes)) {
            target.setAppliedOrderTypes(CommonUtils.APPLIED_ALL);
            return;
        }
        StringJoiner joiner = new StringJoiner(",");
        for(String orderType : orderTypes) {
            if(OrderType.WHOLESALE.toString().equals(orderType)) {
                joiner.add(CommonUtils.WHOLESALE_NAME);
            } else if(OrderType.RETAIL.toString().equals(orderType)) {
                joiner.add(CommonUtils.RETAIL_NAME);
            } else if(OrderType.ONLINE.toString().equals(orderType)) {
                joiner.add(CommonUtils.ONLINE_NAME);
            }
        }
        target.setAppliedOrderTypes(joiner.toString());
    }

    private void populateAppliedWarehouses(PromotionSourceRuleDTO source, PromotionExcelData target) {
        List<Long> warehouseIds = source.getWarehouseIds();
        if(CollectionUtils.isEmpty(warehouseIds)) {
            target.setAppliedWarehouses(CommonUtils.APPLIED_ALL);
            return;
        }
        StringJoiner joiner = new StringJoiner(",");
        for(Long warehouseId : warehouseIds) {
            try {
                WarehouseData warehouseData = logisticService.findByIdAndCompanyId(warehouseId, source.getCompanyId());
                if (warehouseData != null) {
                    joiner.add(warehouseData.getName());
                }
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage() + " ==== warehouseId: " + warehouseId +  ", companyId: " + source.getCompanyId() , e);
            }
        }
        target.setAppliedWarehouses(joiner.toString());
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
