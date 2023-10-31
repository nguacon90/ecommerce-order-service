package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;

import java.util.List;
import java.util.Map;

public interface CartService extends AbstractOrderService<CartModel, CartEntryModel> {

    List<CartModel> findAllOrCreateNewByCreatedByUser(CartInfoParameter cartInfoParameter);

    CartModel findByIdAndCompanyIdAndTypeAndCreateByUser(CartInfoParameter cartInfoParameter);

    CartModel getOrCreateNewCart(CartInfoParameter cartInfoParameter);

    CartModel findByCodeAndUserIdAndCompanyId(String code, Long userId, Long companyId);

    void delete(CartModel cart);

    void refresh(CartModel cart);

    CartModel save(CartModel cart);

    CartModel findByCodeAndCompanyId(String code, Long companyId);

    CartEntryModel findEntryBy(CartModel cartModel, Integer entryNumber);

    CartEntryModel findEntryBy(Long entryId, CartModel cartModel);

    CartEntryModel saveEntry(CartEntryModel entryModel);

    void updateQuantities(CartModel order,  Map<Integer, Long> quantities);

    List<CartEntryModel> findAllEntriesBy(CartModel source);

    CartModel getCartByGuid(CartInfoParameter parameter);

    CartModel findByUserIdAndCompanyIdAndSellSignal(CartInfoParameter parameter);

    CartEntryModel cloneEntry(AbstractOrderEntryModel cartEntryModel, CartModel cartModel);

    void cloneSubOrderEntries(AbstractOrderEntryModel entry, CartEntryModel cloneEntry);

    CartModel findById(Long id);

}
