package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.CommerceAddComboParameter;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.ProductInFreeGiftComboData;
import com.vctek.orderservice.feignclient.dto.BasicProductData;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;

import java.util.List;

public interface AbstractOrderService<O extends AbstractOrderModel, E extends AbstractOrderEntryModel> {
    E addNewEntry(final O order, final Long productId, final long qty, boolean isImport);

    boolean isComboEntry(AbstractOrderEntryModel abstractOrderEntry);

    void addSubOrderEntries(AbstractOrderEntryModel abstractOrderEntry, List<BasicProductData> comboProducts, int qty);

    AbstractOrderModel findByOrderCodeAndCompanyId(String orderCode, Long companyId);

    boolean isValidEntryForPromotion(AbstractOrderEntryModel entryModel);

    void doAddComboToCart(CommerceAddComboParameter commerceAddComboParameter);

    void addSubOrderEntriesToComboEntry(AbstractOrderEntryModel entryModel, List<BasicProductData> subEntries, long quantityToAdd);

    void addProductToComboInPromotion(AbstractOrderEntryModel abstractOrderEntryModel,
                                      List<ProductInFreeGiftComboData> productInFreeGiftComboDataList);

    void updateComboPriceOfEntryWith(AbstractOrderModel model, AbstractOrderEntryModel entryModel, ProductIsCombo productIsCombo, Long productId);

    boolean isSaleOffEntry(OrderEntryDTO orderEntryDTO);

    void normalizeEntryNumbers(final AbstractOrderModel cartModel, boolean isImport);
}
