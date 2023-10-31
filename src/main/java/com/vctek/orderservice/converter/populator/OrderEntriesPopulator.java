package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.AbstractOrderItemImportParameter;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.excel.RowMapperErrorCodes;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceUpdateCartEntryStrategy;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.orderservice.util.OrderSettingType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.orderservice.util.ProductTypeSell;
import com.vctek.util.ComboType;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import com.vctek.util.PermissionCodes;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderEntriesPopulator implements Populator<AbstractOrderItemImportParameter, AbstractOrderModel> {
    public static final int MAXIMUM_ENTRY_ITEMS = 300;
    private ProductService productService;
    private ProductSearchService productSearchService;
    private CartService cartService;
    private OrderService orderService;
    private PermissionFacade permissionFacade;
    private ComboPriceSettingService comboPriceSettingService;
    private LogisticService logisticService;
    private CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;
    private int defaultPageSize = 100;

    @Override
    public void populate(AbstractOrderItemImportParameter parameter, AbstractOrderModel abstractOrderModel) {
        List<OrderItemDTO> orderItems = parameter.getOrderItems();
        if (CollectionUtils.isNotEmpty(orderItems) && orderItems.size() > MAXIMUM_ENTRY_ITEMS) {
            ErrorCodes err = ErrorCodes.IMPORT_OVER_MAXIMUM_ITEM_SIZE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{MAXIMUM_ENTRY_ITEMS});
        }
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCompanyId(abstractOrderModel.getCompanyId());
        searchRequest.setUnpaged(true);
        searchRequest.setPageSize(defaultPageSize * 2);
        searchRequest.setSearchBarcode(true);
        List<AbstractOrderEntryModel> entries = abstractOrderModel.getEntries();
        List<OrderItemDTO> importItems = new ArrayList<>();
        int totalItems = orderItems.size();
        int mode = totalItems % defaultPageSize;
        int totalPage = totalItems / defaultPageSize;
        if (mode != 0) totalPage++;
        int currentPage = 0;
        boolean isValid = true;
        boolean hasEditPricePermission = permissionFacade.hasPermission(PermissionCodes.EDIT_PRICE_ON_ORDER.code(),
                abstractOrderModel.getCompanyId());
        boolean hasEditPriceComboPermission = permissionFacade.hasPermission(PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.code(),
                abstractOrderModel.getCompanyId());
        for (OrderItemDTO orderItem : orderItems) {
            importItems.add(orderItem);
            if (importItems.size() == defaultPageSize) {
                currentPage++;
                StringJoiner joiner = new StringJoiner(CommonUtils.COMMA);
                importItems.stream().forEach(item -> joiner.add(item.getSku()));
                searchRequest.setSku(joiner.toString());
                List<ProductSearchModel> productDataList = productSearchService.findAllByCompanyId(searchRequest);
                Map<String, ProductSearchModel> productMap = populateProductData(productDataList);
                if (!populateValidEntryProduct(importItems, productMap, abstractOrderModel, hasEditPricePermission, hasEditPriceComboPermission)) {
                    isValid = false;
                }
                importItems = new ArrayList<>();
            }
        }

        if (currentPage < totalPage && CollectionUtils.isNotEmpty(importItems)) {
            StringJoiner joiner = new StringJoiner(CommonUtils.COMMA);
            importItems.stream().forEach(item -> joiner.add(item.getSku()));
            searchRequest.setSku(joiner.toString());
            List<ProductSearchModel> productDataList = productSearchService.findAllByCompanyId(searchRequest);
            Map<String, ProductSearchModel> productMap = populateProductData(productDataList);
            if (!populateValidEntryProduct(importItems, productMap, abstractOrderModel, hasEditPricePermission, hasEditPriceComboPermission)) {
                isValid = false;
            }
        }
        if (isValid) {
            addAllEntryToOrder(abstractOrderModel, entries, orderItems, hasEditPricePermission, hasEditPriceComboPermission);
        }
    }

    private void addAllEntryToOrder(AbstractOrderModel abstractOrderModel, List<AbstractOrderEntryModel> entries,
                                    List<OrderItemDTO> importItems, boolean hasEditPricePermission, boolean hasEditPriceComboPermission) {
        for (OrderItemDTO itemDTO : importItems) {
            ProductSearchModel productSearchData = itemDTO.getProductData();
            AbstractOrderEntryModel entry = findEntriesBy(entries, productSearchData.getId());
            int quantity = CommonUtils.strDoubleToInt(itemDTO.getQuantity());
            if (entry != null) {
                Long currentEntryQty = entry.getQuantity();
                Long newEntryQty = entry.getQuantity() + quantity;
                entry.setQuantity(newEntryQty);
                if (StringUtils.isNotBlank(entry.getComboType())) {
                    commerceUpdateCartEntryStrategy.updateSubOrderEntryQty(entry, currentEntryQty.intValue(), newEntryQty.intValue());
                }
                populatePriceWithPermission(abstractOrderModel, productSearchData,
                        entry, itemDTO, hasEditPricePermission, hasEditPriceComboPermission);
                populateDiscount(entry, itemDTO);
                continue;
            }

            AbstractOrderEntryModel entryModel;
            if (abstractOrderModel instanceof CartModel) {
                entryModel = cartService.addNewEntry((CartModel) abstractOrderModel, productSearchData.getId(),
                        quantity, true);
            } else {
                entryModel = orderService.addNewEntry((OrderModel) abstractOrderModel,
                        productSearchData.getId(), quantity, true);
            }
            populateDiscount(entryModel, itemDTO);

            populatePrice(abstractOrderModel, productSearchData, entryModel);

            populatePriceWithPermission(abstractOrderModel, productSearchData, entryModel, itemDTO,
                    hasEditPricePermission, hasEditPriceComboPermission);
        }
    }

    private void populateDiscount(AbstractOrderEntryModel entryModel, OrderItemDTO itemDTO) {
        if (StringUtils.isNotBlank(itemDTO.getDiscount())) {
            entryModel.setDiscount(CommonUtils.strToDouble(itemDTO.getDiscount()));
            entryModel.setDiscountType(itemDTO.getDiscountType());
        }
    }


    private Map<String, ProductSearchModel> populateProductData(List<ProductSearchModel> productDataList) {
        Map<String, ProductSearchModel> map = new HashMap<>();
        productDataList.stream().forEach(p -> {
            map.put(p.getSku(), p);
            map.put(p.getBarcode(),  p);
        });

        return map;
    }

    private boolean populateValidEntryProduct(List<OrderItemDTO> importItems, Map<String, ProductSearchModel> productSearchDataMap,
                                              AbstractOrderModel abstractOrderModel, boolean hasEditPricePermission, boolean hasEditPriceComboPermission) {
        boolean isValid = true;
        String priceType = abstractOrderModel.getPriceType();
        List<Long> productIds = productSearchDataMap.entrySet().stream().map(i -> i.getValue().getId()).collect(Collectors.toList());
        Map<Long, DistributorSetingPriceData> priceSetting = new HashMap<>();
        if (PriceType.DISTRIBUTOR_PRICE.toString().equals(abstractOrderModel.getPriceType())) {
            priceSetting = logisticService.getProductPriceSetting(abstractOrderModel.getDistributorId(),
                    abstractOrderModel.getCompanyId(), productIds);
        }
        for (OrderItemDTO itemDTO : importItems) {
            ProductSearchModel productSearchData = productSearchDataMap.get(itemDTO.getSku());
            if(productSearchData == null) {
                itemDTO.setError(RowMapperErrorCodes.INVALID_PRODUCT_SKU.toString());
                isValid = false;
                continue;
            }

            populatePriceWithPriceTypeDistributor(productSearchData, priceSetting, hasEditPricePermission, hasEditPriceComboPermission, abstractOrderModel, itemDTO);

            boolean validProductInfo = populateProductInfo(itemDTO, productSearchData, priceType,
                    hasEditPricePermission, hasEditPriceComboPermission);
            if (!validProductInfo) {
                isValid = false;
                continue;
            }

            if (PriceType.DISTRIBUTOR_PRICE.toString().equals(abstractOrderModel.getPriceType()) && abstractOrderModel.getDistributorId() == null) {
                itemDTO.setError(RowMapperErrorCodes.INVALID_DISTRIBUTOR_ID.toString());
                isValid = false;
                continue;
            }

            if ((hasEditPricePermission || hasEditPriceComboPermission) && PriceType.RETAIL_PRICE.name().equals(priceType)
                    && productSearchData.getWholesalePrice() != null && StringUtils.isNotBlank(itemDTO.getPrice())
                    && CommonUtils.strToDouble(itemDTO.getPrice()) < productSearchData.getWholesalePrice()) {
                itemDTO.setError(RowMapperErrorCodes.RETAIL_PRICE_MUST_BE_LARGE_WHOLESALE_PRICE.toString());
                isValid = false;
                continue;
            }

            boolean validProductCombo = validProductCombo(itemDTO, productSearchData, hasEditPricePermission, hasEditPriceComboPermission);
            if (!validProductCombo) {
                isValid = false;
                continue;
            }

            itemDTO.setProductData(productSearchData);
        }

        return isValid;
    }

    private boolean populateProductInfo(OrderItemDTO itemDTO, ProductSearchModel productSearchData, String priceType,
                                        boolean hasEditPricePermission, boolean hasEditPriceComboPermission) {
        if (productSearchData == null) {
            itemDTO.setError(RowMapperErrorCodes.INVALID_PRODUCT_SKU.toString());
            return false;
        }

        if(productSearchData.isBaseProduct()) {
            itemDTO.setError(RowMapperErrorCodes.NOT_ACCEPTED_BASE_PRODUCT.toString());
            return false;
        }

        if(ProductTypeSell.STOP_SELLING.toString().equals(productSearchData.getTypeSell())) {
            itemDTO.setError(RowMapperErrorCodes.STOP_SELLING.toString());
            return false;
        }

        if(!productSearchData.isAllowSell()) {
            itemDTO.setError(RowMapperErrorCodes.NOT_ALLOW_SELL.toString());
            return false;
        }

        if (CollectionUtils.isEmpty(productSearchData.getPrices())) {
            itemDTO.setError(RowMapperErrorCodes.PRODUCT_HAS_NOT_PRICE.toString());
            return false;
        }


        if (validateProductInfoWithWholesalePriceType(itemDTO, productSearchData, priceType, hasEditPricePermission, hasEditPriceComboPermission)) {
            return false;
        }

        return true;
    }

    private boolean validateProductInfoWithWholesalePriceType(OrderItemDTO itemDTO, ProductSearchModel productSearchData,
                                                              String priceType, boolean hasEditPricePermission, boolean hasEditPriceComboPermission) {
        if (PriceType.WHOLESALE_PRICE.toString().equals(priceType) && productSearchData.getWholesalePrice() == null) {
            boolean isCombo = StringUtils.isNotBlank(productSearchData.getComboType());
            boolean isNotCombo = StringUtils.isBlank(productSearchData.getComboType());
            if ((hasEditPricePermission || (hasEditPriceComboPermission && isCombo))
                    && StringUtils.isBlank(itemDTO.getPrice())) {
                itemDTO.setError(RowMapperErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.toString());
                return true;
            }

            if (!hasEditPricePermission && !hasEditPriceComboPermission && isCombo) {
                itemDTO.setError(RowMapperErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.toString());
                return true;
            }

            if (!hasEditPricePermission && isNotCombo) {
                itemDTO.setError(RowMapperErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE.toString());
                return true;
            }
        }
        return false;
    }

    private boolean validProductCombo(OrderItemDTO itemDTO, ProductSearchModel productSearchData,
                                      boolean editPricePermission, boolean hasEditPriceComboPermission) {
        boolean isValid = true;
        if (StringUtils.isBlank(productSearchData.getComboType())
                || (!editPricePermission && !hasEditPriceComboPermission)
                || StringUtils.isBlank(itemDTO.getPrice())) return isValid;

        try {
            double priceExcel = Double.parseDouble(itemDTO.getPrice());
            ProductIsCombo productIsCombo = productService.checkIsCombo(productSearchData.getId(), productSearchData.getCompanyId(),
                    CommonUtils.strDoubleToInt(itemDTO.getQuantity()));
            isValid = invalidPriceCombo(priceExcel, productSearchData, itemDTO, productIsCombo);
        } catch (ServiceException e) {
            itemDTO.setError(RowMapperErrorCodes.INVALID_COMBO.toString());
            isValid = false;
        }

        return isValid;
    }

    private boolean invalidPriceCombo(Double price, ProductSearchModel productModel, OrderItemDTO dto, ProductIsCombo productIsCombo) {
        boolean isValid = true;
        if (ComboType.FIXED_COMBO.toString().equals(productModel.getComboType())) {
            double totalPriceInCombo = productIsCombo.getComboProducts().stream().mapToDouble(p -> p.getPrice()).sum();
            if (price > totalPriceInCombo) {
                dto.setError(RowMapperErrorCodes.INVALID_COMBO_PRICE_LESS_THAN.toString());
                dto.setErrorValue(Double.toString(totalPriceInCombo));
                isValid = false;
            }

            OrderSettingModel orderSettingModel = comboPriceSettingService.findByTypeAndCompanyId(OrderSettingType.COMBO_PRICE_SETTING.code(), productModel.getCompanyId());
            if (orderSettingModel != null) {
                double minimumPriceCombo = CurrencyUtils.computeValue(orderSettingModel.getAmount(), orderSettingModel.getType(), totalPriceInCombo);
                if (price < minimumPriceCombo) {
                    dto.setError(RowMapperErrorCodes.INVALID_COMBO_PRICE_LARGER_THAN.toString());
                    dto.setErrorValue(Double.toString(minimumPriceCombo));
                    isValid = false;
                }
            }

        }
        return isValid;
    }

    private void populatePrice(AbstractOrderModel abstractOrderModel, ProductSearchModel data, AbstractOrderEntryModel entryModel) {
        if (StringUtils.isBlank(data.getComboType())) {
            entryModel.setBasePrice(data.getPrices().get(0).getPrice());
            entryModel.setOriginBasePrice(data.getPrices().get(0).getPrice());
            return;
        }

        ProductIsCombo productIsCombo = productService.checkIsCombo(entryModel.getProductId(), abstractOrderModel.getCompanyId(),
                entryModel.getQuantity().intValue());
        if (!productIsCombo.isCombo()) {
            entryModel.setBasePrice(data.getPrices().get(0).getPrice());
            entryModel.setOriginBasePrice(data.getPrices().get(0).getPrice());
            return;
        }

        entryModel.setBasePrice(productIsCombo.getPrice());
        entryModel.setOriginBasePrice(productIsCombo.getPrice());
        entryModel.setComboType(productIsCombo.getComboType());
        if (ComboType.FIXED_COMBO.toString().equals(productIsCombo.getComboType())) {
            cartService.addSubOrderEntries(entryModel, productIsCombo.getComboProducts(), entryModel.getQuantity().intValue());
        }
    }

    private void populatePriceWithPermission(AbstractOrderModel abstractOrderModel, ProductSearchModel data,
                                             AbstractOrderEntryModel entryModel, OrderItemDTO itemDTO, boolean hasEditPrice, boolean hasEditPriceCombo) {
        if (isNotWholesalePriceType(abstractOrderModel)) {
            populatePriceNotWholesalePricee(abstractOrderModel, data, entryModel, itemDTO, hasEditPrice, hasEditPriceCombo);
            return;
        }

        double wholesalePrice = CommonUtils.readValue(data.getWholesalePrice());
        if(hasEditPrice && StringUtils.isNotBlank(itemDTO.getPrice())) {
            entryModel.setBasePrice(Double.parseDouble(itemDTO.getPrice()));
            entryModel.setOriginBasePrice(wholesalePrice);
            return;
        }

        if(isEditExcelPriceCombo(data, itemDTO, hasEditPriceCombo)) {
            entryModel.setBasePrice(Double.parseDouble(itemDTO.getPrice()));
            entryModel.setOriginBasePrice(wholesalePrice);
            return;
        }

        if((!hasEditPrice && !hasEditPriceCombo) || StringUtils.isBlank(itemDTO.getPrice())) {
            entryModel.setBasePrice(wholesalePrice);
            entryModel.setOriginBasePrice(wholesalePrice);
            return;
        }

        if (!hasEditPrice && StringUtils.isBlank(data.getComboType())) {
            entryModel.setBasePrice(wholesalePrice);
            entryModel.setOriginBasePrice(wholesalePrice);
        }
    }

    private void populatePriceNotWholesalePricee(AbstractOrderModel abstractOrderModel, ProductSearchModel data, AbstractOrderEntryModel entryModel, OrderItemDTO itemDTO, boolean hasEditPrice, boolean hasEditPriceCombo) {
        if(PriceType.DISTRIBUTOR_PRICE.toString().equals(abstractOrderModel.getPriceType()) && StringUtils.isNotBlank(itemDTO.getPrice())) {
            entryModel.setRecommendedRetailPrice(itemDTO.getRecommendedRetailPrice());
            entryModel.setBasePrice(Double.parseDouble(itemDTO.getPrice()));
            return;
        }
        if(hasEditPrice && StringUtils.isNotBlank(itemDTO.getPrice())) {
            entryModel.setBasePrice(Double.parseDouble(itemDTO.getPrice()));
            return;
        }

        if(isEditExcelPriceCombo(data, itemDTO, hasEditPriceCombo)) {
            entryModel.setBasePrice(Double.parseDouble(itemDTO.getPrice()));
        }
    }

    private boolean isEditExcelPriceCombo(ProductSearchModel data, OrderItemDTO itemDTO, boolean hasEditPriceCombo) {
        return hasEditPriceCombo && StringUtils.isNotBlank(data.getComboType()) &&
                StringUtils.isNotBlank(itemDTO.getPrice());
    }

    private boolean isNotWholesalePriceType(AbstractOrderModel abstractOrderModel) {
        return !OrderType.ONLINE.name().equals(abstractOrderModel.getType())
                || (!PriceType.WHOLESALE_PRICE.name().equals(abstractOrderModel.getPriceType()));
    }

    private AbstractOrderEntryModel findEntriesBy(List<AbstractOrderEntryModel> entries, Long id) {
        for (AbstractOrderEntryModel entry : entries) {
            if (entry.getProductId() != null && entry.getProductId().equals(id)) {
                return entry;
            }
        }

        return null;
    }

    private void populatePriceWithPriceTypeDistributor(ProductSearchModel productSearchModel, Map<Long, DistributorSetingPriceData> priceSetting,
                                                       boolean hasEditPrice, boolean hasEditPriceCombo, AbstractOrderModel model, OrderItemDTO dto) {
        if (!PriceType.DISTRIBUTOR_PRICE.toString().equals(model.getPriceType())) return;
        Double price = productSearchModel.getPrices().get(0).getPrice();
        dto.setRecommendedRetailPrice(price);

        if((!hasEditPrice && !hasEditPriceCombo) || StringUtils.isBlank(dto.getPrice())
            || (!hasEditPrice && StringUtils.isBlank(productSearchModel.getComboType()))) {
            dto.setPrice(price.toString());

            if (!priceSetting.containsKey(productSearchModel.getId())) {
                return;
            }

            DistributorSetingPriceData setingPriceData = priceSetting.get(productSearchModel.getId());
            if (setingPriceData.getRecommendedRetailPrice() != null) {
                dto.setRecommendedRetailPrice(setingPriceData.getRecommendedRetailPrice());
            }
            if (StringUtils.isBlank(productSearchModel.getComboType())) {
                Double basePrice = logisticService.calculateDistributorSettingPrice(setingPriceData, price);
                dto.setPrice(basePrice.toString());
                return;
            }
            ProductIsCombo productIsCombo = productService.checkIsCombo(productSearchModel.getId(), productSearchModel.getCompanyId(),
                    CommonUtils.strDoubleToInt(dto.getQuantity()));
            price = productIsCombo.getPrice();
            if (setingPriceData.getRecommendedRetailPrice() == null) {
                dto.setRecommendedRetailPrice(price);
            }
            Double basePrice = logisticService.calculateDistributorSettingPrice(setingPriceData, price);
            boolean isValidCombo = invalidPriceCombo(basePrice, productSearchModel, dto, productIsCombo);
            if (isValidCombo) {
                dto.setPrice(basePrice.toString());
            }
        }
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    @Autowired
    public void setComboPriceSettingService(ComboPriceSettingService comboPriceSettingService) {
        this.comboPriceSettingService = comboPriceSettingService;
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setCommerceUpdateCartEntryStrategy(CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy) {
        this.commerceUpdateCartEntryStrategy = commerceUpdateCartEntryStrategy;
    }
}
