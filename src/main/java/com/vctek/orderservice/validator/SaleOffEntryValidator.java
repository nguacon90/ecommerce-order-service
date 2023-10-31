package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.util.CommonUtils;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SaleOffEntryValidator implements Validator<CommerceAbstractOrderParameter> {
    protected InventoryService inventoryService;
    protected EntryRepository entryRepository;
    protected ProductService productService;

    @Override
    public void validate(CommerceAbstractOrderParameter parameter) {
        if(!validateSaleOffParam(parameter.isSaleOff())) {
            return;
        }

        AbstractOrderModel order = parameter.getOrder();
        AbstractOrderEntryModel entryModel = getValidatedEntry(parameter, order);
        validateProductEntry(parameter, entryModel);

        validateStockSaleOff(parameter, entryModel, false, entryModel.getQuantity());
    }

    protected boolean validateSaleOffParam(boolean saleOff) {
        return saleOff;
    }

    private void validateProductEntry(CommerceAbstractOrderParameter parameter, AbstractOrderEntryModel entryModel) {
        if (!parameter.isSaleOff()) return;
        if(isComboEntry(entryModel)) {
            ErrorCodes err = ErrorCodes.CAN_NOT_SALE_OFF_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Long productId = entryModel.getProductId();
        if(productService.isFnB(productId)) {
            ErrorCodes err = ErrorCodes.CAN_NOT_SALE_OFF_FNB_PRODUCT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateStockSaleOff(CommerceAbstractOrderParameter parameter, AbstractOrderEntryModel validateEntryModel, boolean ignoreValidateEntry, Long quantity) {
        int brokenQty = getBrokenStock(parameter.getOrder(), validateEntryModel);

        List<AbstractOrderEntryModel> saleOffEntries = entryRepository.findAllByOrderAndSaleOffAndProductId(parameter.getOrder(), true, validateEntryModel.getProductId());
        if(ignoreValidateEntry) {
            saleOffEntries = saleOffEntries.stream().filter(e -> !e.getId().equals(validateEntryModel.getId()))
                    .collect(Collectors.toList());
        }

        if(CollectionUtils.isNotEmpty(saleOffEntries)) {
            long totalSaleOff = saleOffEntries.stream().mapToLong(AbstractOrderEntryModel::getQuantity).sum();
            brokenQty -= totalSaleOff;
        }

        if(quantity > brokenQty) {
            ErrorCodes err = ErrorCodes.ENTRY_QUANTITY_OVER_BROKEN_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{brokenQty});
        }
    }

    protected int getBrokenStock(AbstractOrderModel order, AbstractOrderEntryModel validateEntryModel) {
        ProductStockData brokenStock = inventoryService.getBrokenStock(validateEntryModel.getProductId(), order.getCompanyId(), order.getWarehouseId());
        int brokenQty = CommonUtils.readValue(brokenStock.getQuantity());
        if(brokenQty <= 0) {
            ErrorCodes err = ErrorCodes.PRODUCT_OUT_OF_BROKEN_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        return brokenQty;
    }

    protected abstract boolean isComboEntry(AbstractOrderEntryModel entryModel);

    protected AbstractOrderEntryModel getValidatedEntry(CommerceAbstractOrderParameter parameter, AbstractOrderModel order) {
        AbstractOrderEntryModel entryModel = entryRepository.findByIdAndOrder(parameter.getEntryId(), order);
        if(entryModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ENTRY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        return entryModel;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
