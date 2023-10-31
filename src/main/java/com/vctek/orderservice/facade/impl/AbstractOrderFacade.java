package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.RefreshCartRequest;
import com.vctek.orderservice.dto.request.ToppingItemRequest;
import com.vctek.orderservice.excel.OrderItemExcelFileReader;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.service.PaymentTransactionService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.service.ToppingOptionService;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.sync.MutexFactory;
import com.vctek.util.ComboType;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractOrderFacade {
    protected ProductService productService;
    protected OrderItemExcelFileReader orderItemExcelFileReader;
    protected Populator<AbstractOrderItemImportParameter, AbstractOrderModel> orderEntriesPopulator;
    protected Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
    protected MutexFactory<String> mutexFactory;
    protected Validator<List<OrderItemDTO>> importOrderItemValidator;
    protected ToppingOptionService toppingOptionService;
    protected PaymentTransactionService paymentTransactionService;
    protected CustomerService customerService;

    protected void validateAbstractOrder(AbstractOrderModel orderModel) {
        if (orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected boolean shouldClearCartData(AbstractOrderModel abstractOrderModel, RefreshCartRequest refreshCartRequest) {
        Long currentCartCompanyId = abstractOrderModel.getCompanyId();
        Long currentCartWarehouseId = abstractOrderModel.getWarehouseId();
        Long updatedCompanyId = refreshCartRequest.getCompanyId();
        Long updateWarehouseId = refreshCartRequest.getWarehouseId();
        if (updatedCompanyId != null && !updatedCompanyId.equals(currentCartCompanyId)) {
            abstractOrderModel.setCompanyId(updatedCompanyId);
            abstractOrderModel.setWarehouseId(refreshCartRequest.getWarehouseId());
            return true;
        }

        if (updateWarehouseId != null && !updateWarehouseId.equals(currentCartWarehouseId)) {
            abstractOrderModel.setWarehouseId(updateWarehouseId);
        }

        return false;
    }

    protected void validateAbstractOrderEntry(AbstractOrderEntryModel entryModel) {
        if (entryModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateToppingOptionModel(ToppingOptionModel model) {
        if (model == null) {
            ErrorCodes err = ErrorCodes.INVALID_TOPPING_OPTION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected ProductInComboData getValidatedProductInEntryCombo(AddSubOrderEntryRequest request, ComboData comboData,
                                                                 AbstractOrderEntryModel entryCombo) {

        Set<SubOrderEntryModel> subOrderEntries = entryCombo.getSubOrderEntries();
        int totalItems = getTotalItemOfEntry(entryCombo);
        if (request.isUpdateQuantity()) {
            Optional<SubOrderEntryModel> subOrderEntryOptional = subOrderEntries.stream().filter(sub -> sub.getProductId().equals(request.getProductId())).findFirst();
            if (subOrderEntryOptional.isPresent()) {
                SubOrderEntryModel subOrderEntryModel = subOrderEntryOptional.get();
                totalItems = totalItems - subOrderEntryModel.getQuantity();
            }
        }

        int newSubOrderEntryQty = totalItems + request.getQuantity() * entryCombo.getQuantity().intValue();
        int maxTotalItems = getMaxTotalItemsOfCombo(comboData, entryCombo);
        if (newSubOrderEntryQty > maxTotalItems) {
            ErrorCodes err = ErrorCodes.PRODUCT_EXCEED_ALLOWED_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        StringBuilder productIds = new StringBuilder();
        productIds.append(request.getProductId());

        for (SubOrderEntryModel subOrderEntryModel : subOrderEntries) {
            productIds.append(CommonUtils.COMMA).append(subOrderEntryModel.getProductId());
            if (comboData.getComboType().equals(ComboType.ONE_GROUP.toString()) &&
                    Boolean.FALSE.equals(comboData.isDuplicateSaleProduct()) &&
                    subOrderEntryModel.getProductId().equals(request.getProductId())) {
                ErrorCodes err = ErrorCodes.EXISTED_PRODUCT_IN_COMBO;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            if (comboData.getComboType().equals(ComboType.MULTI_GROUP.toString()) &&
                    subOrderEntryModel.getProductId().equals(request.getProductId())) {
                ErrorCodes err = ErrorCodes.EXISTED_PRODUCT_IN_COMBO;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

        }
        List<ProductInComboData> productList = productService.getProductInCombo(request.getComboId(),
                request.getCompanyId(), request.getProductId().toString());
        if (CollectionUtils.isEmpty(productList)) {
            ErrorCodes err = ErrorCodes.NOT_EXISTED_PRODUCT_IN_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Optional<ProductInComboData> productInComboOptional = productList.stream().filter(p -> p.getId() != null &&
                p.getId().equals(request.getProductId())).findFirst();
        if (!productInComboOptional.isPresent()) {
            ErrorCodes err = ErrorCodes.NOT_EXISTED_PRODUCT_IN_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        checkProductExistInGroupCombo(comboData, productIds);

        return productInComboOptional.get();


    }

    protected int getMaxTotalItemsOfCombo(ComboData comboData, AbstractOrderEntryModel entryCombo) {
        return (int) (comboData.getTotalItemQuantity() * entryCombo.getQuantity());
    }

    protected int getTotalItemOfEntry(AbstractOrderEntryModel abstractOrderEntryModel) {
        return abstractOrderEntryModel.getSubOrderEntries().stream().filter(soe -> soe.getQuantity() != null)
                .mapToInt(SubOrderEntryModel::getQuantity).sum();
    }

    protected void checkProductExistInGroupCombo(ComboData comboData, StringBuilder productIds) {
        if (comboData.getComboType().equals(ComboType.MULTI_GROUP.toString())) {
            Boolean productExistInGroupCombo = productService.productExistInGroupCombo(comboData.getId(), productIds.toString());
            if (productExistInGroupCombo == true) {
                ErrorCodes err = ErrorCodes.EXISTED_PRODUCT_IN_GROUP_COMBO;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    protected OrderImportData createErrorOrderImportData(List<OrderItemDTO> errorItems) {
        OrderImportData orderImportData = new OrderImportData();
        orderImportData.setItemDTO(errorItems);
        orderImportData.setHasError(true);
        return orderImportData;
    }

    protected void validateCurrencyType(CurrencyType currencyType) {
        if (currencyType == null) {
            ErrorCodes err = ErrorCodes.INVALID_DISCOUNT_TYPE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected ToppingItemParameter poulateToppingItemParameter(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel abstractOrderEntryModel, ToppingItemRequest request) {
        ToppingItemParameter parameter = new ToppingItemParameter();
        parameter.setAbstractOrderModel(abstractOrderModel);
        parameter.setAbstractOrderEntryModel(abstractOrderEntryModel);
        ToppingOptionModel toppingOptionModel = toppingOptionService.findByIdAndOrderEntry(request.getToppingOptionId(), abstractOrderEntryModel);
        validateToppingOptionModel(toppingOptionModel);
        parameter.setToppingOptionModel(toppingOptionModel);
        parameter.setToppingItemId(request.getId());
        parameter.setQuantity(request.getQuantity());
        return parameter;
    }

    protected void validatePriceOrderEntry(AbstractOrderModel model, OrderEntryDTO orderEntryDTO) {
        if (PriceType.RETAIL_PRICE.name().equals(model.getPriceType()) && OrderType.ONLINE.name().equals(model.getType())) {
            ProductSearchRequest searchRequest = new ProductSearchRequest();
            searchRequest.setIds(orderEntryDTO.getProductId().toString());
            searchRequest.setCompanyId(model.getCompanyId());
            searchRequest.setPageSize(1);
            List<ProductSearchData> productSearchData = productService.search(searchRequest);
            if (CollectionUtils.isNotEmpty(productSearchData) && productSearchData.get(0).getWholesalePrice() != null
                    && orderEntryDTO.getPrice() < productSearchData.get(0).getWholesalePrice()) {
                ErrorCodes err = ErrorCodes.RETAIL_PRICE_MUST_BE_LARGE_WHOLESALE_PRICE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    protected void validateDistributorPriceOrder(AbstractOrderModel model) {
        if (OrderType.ONLINE.name().equals(model.getType()) && PriceType.DISTRIBUTOR_PRICE.name().equals(model.getPriceType())
                && model.getDistributorId() == null) {
            ErrorCodes err = ErrorCodes.INVALID_DISTRIBUTOR_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateCustomer(CustomerRequest request, Long companyId) {
        if (request == null || request.getId() == null) return;
        CustomerData customerData = customerService.getBasicCustomerInfo(companyId, request.getId());
        if (customerData == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }


    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setOrderItemExcelFileReader(OrderItemExcelFileReader orderItemExcelFileReader) {
        this.orderItemExcelFileReader = orderItemExcelFileReader;
    }

    @Autowired
    public void setOrderEntriesPopulator(Populator<AbstractOrderItemImportParameter, AbstractOrderModel> orderEntriesPopulator) {
        this.orderEntriesPopulator = orderEntriesPopulator;
    }

    @Autowired
    public void setOrderEntryConverter(Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter) {
        this.orderEntryConverter = orderEntryConverter;
    }

    @Autowired
    public void setMutexFactory(MutexFactory<String> mutexFactory) {
        this.mutexFactory = mutexFactory;
    }

    @Autowired
    public void setImportOrderItemValidator(Validator<List<OrderItemDTO>> importOrderItemValidator) {
        this.importOrderItemValidator = importOrderItemValidator;
    }

    @Autowired
    public void setToppingOptionService(ToppingOptionService toppingOptionService) {
        this.toppingOptionService = toppingOptionService;
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }
}
