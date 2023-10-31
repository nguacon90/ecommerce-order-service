package com.vctek.orderservice.service;

import com.vctek.orderservice.model.ToppingItemModel;
import com.vctek.orderservice.model.ToppingOptionModel;

import java.util.List;
import java.util.Set;

public interface ToppingItemService {
    ToppingItemModel findByIdAndToppingOption(Long id, ToppingOptionModel toppingOptionModel);

    void delete(ToppingItemModel toppingItemModel);

    double totalDiscountToppingItem(List<ToppingOptionModel> toppingOptionModels);

    ToppingItemModel findById(Long id);

    void saveAll(List<ToppingItemModel> updateItems);

    Set<ToppingItemModel> findAllByOrderId(Long orderId);

    Set<ToppingItemModel> findAllByEntryId(Long entryId);

    Set<ToppingItemModel> findAllByToppingOptionModel(ToppingOptionModel toppingOptionModel);
}
