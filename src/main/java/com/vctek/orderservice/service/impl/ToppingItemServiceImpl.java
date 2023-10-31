package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.ToppingItemModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.ToppingItemService;
import com.vctek.orderservice.util.CurrencyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ToppingItemServiceImpl implements ToppingItemService {
    private ToppingItemRepository repository;

    public ToppingItemServiceImpl(ToppingItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public ToppingItemModel findByIdAndToppingOption(Long id, ToppingOptionModel toppingOptionModel) {
        return repository.findByIdAndToppingOptionModel(id, toppingOptionModel);
    }

    @Override
    @Transactional
    public void delete(ToppingItemModel toppingItemModel) {
        repository.delete(toppingItemModel);
    }

    @Override
    public double totalDiscountToppingItem(List<ToppingOptionModel> toppingOptionModels) {
        double totalDiscount = 0;
        for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
            Set<ToppingItemModel> toppingItemModels = this.findAllByToppingOptionModel(toppingOptionModel);
            for (ToppingItemModel model : toppingItemModels) {
                if (model.getDiscount() != null && StringUtils.isNotEmpty(model.getDiscountType())) {
                    Double amount = model.getBasePrice() * model.getQuantity() * toppingOptionModel.getQuantity();
                    totalDiscount += CurrencyUtils.computeValue(model.getDiscount(), model.getDiscountType(), amount);
                }
            }
        }
        return totalDiscount;
    }

    @Override
    public ToppingItemModel findById(Long id) {
        Optional<ToppingItemModel> option = repository.findById(id);
        return option.isPresent() ? option.get() : null;
    }

    @Override
    public void saveAll(List<ToppingItemModel> updateItems) {
        repository.saveAll(updateItems);
    }

    @Override
    public Set<ToppingItemModel> findAllByOrderId(Long orderId) {
        return repository.findAllByOrderId(orderId);
    }

    @Override
    public Set<ToppingItemModel> findAllByEntryId(Long entryId) {
        return repository.findAllByEntryId(entryId);
    }

    @Override
    public Set<ToppingItemModel> findAllByToppingOptionModel(ToppingOptionModel toppingOptionModel) {
        List<ToppingItemModel> items = repository.findAllByToppingOptionModel(toppingOptionModel);
        if(CollectionUtils.isNotEmpty(items)) {
            return new HashSet<>(items);
        }
        return new HashSet<>();
    }
}
