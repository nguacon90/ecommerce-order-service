package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderEntry;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ToppingItemModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.repository.ToppingOptionRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.ProductData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("exchangeOrderDocumentPopulator")
public class ExchangeOrderDocumentPopulator implements Populator<OrderModel, ExchangeOrder> {

    private OrderEntryRepository orderEntryRepository;
    private CalculationService calculationService;
    private ProductService productService;
    private ToppingOptionRepository toppingOptionRepository;
    private ToppingItemRepository toppingItemRepository;

    @Override
    public void populate(OrderModel exchangeOrder, ExchangeOrder exchangeOrderDoc) {
        if (exchangeOrder != null) {
            exchangeOrderDoc.setCode(exchangeOrder.getCode());
            exchangeOrderDoc.setFinalPrice(exchangeOrder.getFinalPrice());
            exchangeOrderDoc.setEntries(populateExchangeOrderEntries(exchangeOrder));
            exchangeOrderDoc.setWarehouseId(exchangeOrder.getWarehouseId());
            exchangeOrderDoc.setVatExchange(exchangeOrder.getTotalTax());
        }
    }

    protected List<ReturnOrderEntry> populateExchangeOrderEntries(OrderModel exchangeOrder) {
        List<ReturnOrderEntry> entries = new ArrayList<>();
        List<OrderEntryModel> orderEntries = orderEntryRepository.findAllByOrder(exchangeOrder);
        if (CollectionUtils.isNotEmpty(orderEntries)) {
            for (OrderEntryModel entry : orderEntries) {
                ReturnOrderEntry returnOrderEntry = new ReturnOrderEntry();
                returnOrderEntry.setProductId(entry.getProductId());
                returnOrderEntry.setQuantity(entry.getQuantity());
                returnOrderEntry.setPrice(entry.getBasePrice());
                returnOrderEntry.setDiscount(calculationService.calculateFinalDiscountOfEntry(entry));
                returnOrderEntry.setProductVat(entry.getVat());
                returnOrderEntry.setProductVatType(entry.getVatType());
                boolean isCombo = false;
                if(entry.getComboType() != null) {
                    returnOrderEntry.setComboId(entry.getProductId());
                    isCombo = true;
                }
                populateProductData(returnOrderEntry, entry.getProductId(), isCombo);
                entries.add(returnOrderEntry);
                populateTopping(entries, entry);
            }
        }
        return entries;
    }

    private void populateTopping(List<ReturnOrderEntry> entries, OrderEntryModel exchangeEntry) {
        List<ToppingOptionModel> optionModels = toppingOptionRepository.findAllByOrderEntry(exchangeEntry);
        if (CollectionUtils.isNotEmpty(optionModels)) {
            for (ToppingOptionModel optionModel : optionModels) {
                List<ToppingItemModel> itemModels = toppingItemRepository.findAllByToppingOptionModel(optionModel);
                for (ToppingItemModel itemModel : itemModels) {
                    ReturnOrderEntry returnOrderEntry = new ReturnOrderEntry();
                    returnOrderEntry.setProductId(itemModel.getProductId());
                    returnOrderEntry.setQuantity((long) (itemModel.getQuantity() * optionModel.getQuantity()));
                    returnOrderEntry.setPrice(itemModel.getBasePrice());
                    returnOrderEntry.setDiscount(itemModel.getDiscountOrderToItem());
                    returnOrderEntry.setProductVat(itemModel.getVat());
                    returnOrderEntry.setProductVatType(itemModel.getVatType());
                    populateProductData(returnOrderEntry, itemModel.getProductId(), false);
                    entries.add(returnOrderEntry);
                }
            }
        }
    }

    private void populateProductData(ReturnOrderEntry returnOrderEntry, Long productId, boolean isCombo) {
        ProductData productDetailData = productService.getBasicProductDetail(productId);
        if (productDetailData != null) {
            if(isCombo) {
                returnOrderEntry.setComboName(productDetailData.getName());
                returnOrderEntry.setComboSku(productDetailData.getSku());
            }
            returnOrderEntry.setProductName(productDetailData.getName());
            returnOrderEntry.setProductSku(productDetailData.getSku());
            returnOrderEntry.setName(productDetailData.getName());
            returnOrderEntry.setdType(productDetailData.getdType());
            returnOrderEntry.setSupplierProductName(productDetailData.getSupplierProductName());
        }
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setToppingOptionRepository(ToppingOptionRepository toppingOptionRepository) {
        this.toppingOptionRepository = toppingOptionRepository;
    }

    @Autowired
    public void setToppingItemRepository(ToppingItemRepository toppingItemRepository) {
        this.toppingItemRepository = toppingItemRepository;
    }
}
