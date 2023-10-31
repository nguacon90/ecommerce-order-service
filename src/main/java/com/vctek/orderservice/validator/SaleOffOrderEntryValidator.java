package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("saleOffOrderEntryValidator")
public class SaleOffOrderEntryValidator extends SaleOffEntryValidator {
    private OrderService orderService;
    private CompanyClient companyClient;

    @Override
    public boolean validateSaleOffParam(boolean saleOff) {
        return true;
    }

    @Override
    public void validateStockSaleOff(CommerceAbstractOrderParameter parameter, AbstractOrderEntryModel validateEntryModel,
                                     boolean ignoreValidateEntry, Long quantity) {
        if (!parameter.isSaleOff()) {
            validateSellLessZero(parameter, validateEntryModel);
            return;
        }
        int brokenStock = getBrokenStock(parameter.getOrder(), validateEntryModel);
        if(validateEntryModel.getQuantity() > brokenStock) {
            ErrorCodes err = ErrorCodes.ENTRY_QUANTITY_OVER_BROKEN_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{brokenStock});
        }
    }

    private void validateSellLessZero(CommerceAbstractOrderParameter parameter, AbstractOrderEntryModel entryToUpdate) {
        if (Boolean.TRUE.equals(parameter.isSaleOff())) return;
        AbstractOrderModel orderModel = parameter.getOrder();
        Boolean sellLessZero = companyClient.checkSellLessZero(orderModel.getCompanyId());
        if (Boolean.FALSE.equals(sellLessZero)) {
            ProductStockData availableStockData = inventoryService.getAvailableStock(entryToUpdate.getProductId(),
                    orderModel.getCompanyId(), orderModel.getWarehouseId());
            if (availableStockData == null || availableStockData.getQuantity() < entryToUpdate.getQuantity()) {
                ErrorCodes err = ErrorCodes.SWITCH_SALE_OFF_QUANTITY_OVER_AVAILABLE_STOCK;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    @Override
    protected boolean isComboEntry(AbstractOrderEntryModel entryModel) {
        return orderService.isComboEntry(entryModel);
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setCompanyClient(CompanyClient companyClient) {
        this.companyClient = companyClient;
    }

    @Autowired
    @Override
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
