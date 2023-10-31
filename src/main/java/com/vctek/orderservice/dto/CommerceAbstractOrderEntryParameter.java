package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;

public class CommerceAbstractOrderEntryParameter {
    private AbstractOrderEntryModel orderEntryModel;
    private AbstractOrderModel orderModel;
    private ProductInComboData productInComboData;
    private ComboData comboData;

    public CommerceAbstractOrderEntryParameter(AbstractOrderEntryModel orderEntryModel, AbstractOrderModel orderModel) {
        this.orderEntryModel = orderEntryModel;
        this.orderModel = orderModel;
    }


    public AbstractOrderEntryModel getOrderEntryModel() {
        return orderEntryModel;
    }

    public ProductInComboData getProductInComboData() {
        return productInComboData;
    }

    public void setProductInComboData(ProductInComboData productInComboData) {
        this.productInComboData = productInComboData;
    }

    public AbstractOrderModel getOrderModel() {
        return orderModel;
    }

    public ComboData getComboData() {
        return comboData;
    }

    public void setComboData(ComboData comboData) {
        this.comboData = comboData;
    }
}
