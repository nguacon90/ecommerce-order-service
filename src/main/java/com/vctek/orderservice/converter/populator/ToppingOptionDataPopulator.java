package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.ToppingItemData;
import com.vctek.orderservice.dto.ToppingOptionData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.ToppingItemModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.ProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToppingOptionDataPopulator implements Populator<ToppingOptionModel, ToppingOptionData> {
    private ProductService productService;

    @Override
    public void populate(ToppingOptionModel toppingOptionModel, ToppingOptionData toppingOptionData) {
        toppingOptionData.setId(toppingOptionModel.getId());
        toppingOptionData.setIce(toppingOptionModel.getIce());
        toppingOptionData.setSugar(toppingOptionModel.getSugar());
        toppingOptionData.setQuantity(toppingOptionModel.getQuantity());
        populateToppingItems(toppingOptionModel, toppingOptionData);
    }

    private void populateToppingItems(ToppingOptionModel toppingOptionModel, ToppingOptionData toppingData) {
        List<ToppingItemData> toppingItemData = new ArrayList<>();
        AbstractOrderEntryModel orderEntry = toppingOptionModel.getOrderEntry();
        int optQty = CommonUtils.getIntValue(toppingOptionModel.getQuantity());
        for (ToppingItemModel model : toppingOptionModel.getToppingItemModels()) {
            ToppingItemData toppingItem = new ToppingItemData();
            toppingItem.setId(model.getId());
            int itemQty = CommonUtils.getIntValue(model.getQuantity());
            toppingItem.setQuantity(itemQty);
            toppingItem.setTotalQuantity(itemQty * optQty);
            double price = CommonUtils.getDoubleValue(model.getBasePrice());
            toppingItem.setPrice(price);
            toppingItem.setTotalPrice(price * toppingItem.getTotalQuantity());
            ProductData productData = productService.getBasicProductDetail(model.getProductId());
            if(orderEntry instanceof CartEntryModel) {
                toppingItem.setVat(productData.getVat());
                toppingItem.setVatType(productData.getVatType());
            } else {
                toppingItem.setVat(model.getVat());
                toppingItem.setVatType(model.getVatType());
            }
            toppingItem.setProductId(productData.getId());
            toppingItem.setProductName(productData.getName());
            toppingItem.setDiscountOrderToItem(CommonUtils.getDoubleValue(model.getDiscountOrderToItem()));
            toppingItem.setDiscount(CommonUtils.getDoubleValue(model.getDiscount()));
            toppingItem.setDiscountType(model.getDiscountType());
            toppingItem.setRewardAmount(model.getRewardAmount());
            Double fixedDiscount = CommonUtils.calculateValueByCurrencyType(toppingItem.getTotalPrice(), toppingItem.getDiscount(),
                    toppingItem.getDiscountType());
            toppingItem.setFixedDiscount(fixedDiscount);
            toppingItemData.add(toppingItem);
        }
        toppingData.setToppingItems(toppingItemData);
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
