package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.strategy.EntryMergeStrategy;
import com.vctek.util.ComboType;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefaultEntryMergeStrategy implements EntryMergeStrategy {
    private CartService cartService;

    @Override
    public AbstractOrderEntryModel getEntryToMerge(List<AbstractOrderEntryModel> toEntries, AbstractOrderEntryModel newEntry) {
        if(CollectionUtils.isEmpty(toEntries)) {
            return null;
        }

        if(newEntry.isGiveAway()) {
            return null;
        }

        return toEntries.stream()
                .filter(e -> !newEntry.equals(e))
                .filter(e -> {
                    if(e.isGiveAway()) {
                        return false;
                    }
                    if(!e.getProductId().equals(newEntry.getProductId())) {
                        return false;
                    }
                    return checkValidComboToMerge(e, newEntry);
                }).sorted((e1, e2) -> 0)
                .findFirst().orElse(null);
    }

    private boolean checkValidComboToMerge(AbstractOrderEntryModel entry, AbstractOrderEntryModel newEntry) {
        if(ComboType.FIXED_COMBO.toString().equalsIgnoreCase(entry.getComboType()) &&
            ComboType.FIXED_COMBO.toString().equalsIgnoreCase(newEntry.getComboType())) {
            return true;
        }

        boolean entryIsCombo = cartService.isComboEntry(entry);
        boolean newEntryIsCombo = cartService.isComboEntry(newEntry);
        if(!entryIsCombo && !newEntryIsCombo) {
            return true;
        }

        if(entryIsCombo && newEntryIsCombo) {
            return isTheSameComboToMerge(entry, newEntry);
        }

        return false;
    }

    private boolean isTheSameComboToMerge(AbstractOrderEntryModel entry, AbstractOrderEntryModel newEntry) {
        if(StringUtils.isNotBlank(entry.getComboType()) &&
                !entry.getComboType().equalsIgnoreCase(newEntry.getComboType())) {
            return false;
        }

        Set<SubOrderEntryModel> candidateSubOrderEntries = entry.getSubOrderEntries();
        Set<SubOrderEntryModel> subOrderEntries = newEntry.getSubOrderEntries();
        if(candidateSubOrderEntries.size() != subOrderEntries.size()) {
            return false;
        }

        Map<Long, Long> candidateProductMap = new HashMap<>();
        candidateSubOrderEntries.forEach(csoe -> {
            long unitQty = CommonUtils.readValue(csoe.getQuantity()) / CommonUtils.readValue(entry.getQuantity());
            candidateProductMap.put(csoe.getProductId(), unitQty);
        });

        Optional<SubOrderEntryModel> existedDiffSubEntryOptional = subOrderEntries.stream().filter(soe -> {
            if (!candidateProductMap.containsKey(soe.getProductId())) {
                return true;
            }

            Long candidateUnitQty = candidateProductMap.get(soe.getProductId());
            long unitQty = CommonUtils.readValue(soe.getQuantity()) / CommonUtils.readValue(newEntry.getQuantity());
            if (candidateUnitQty != unitQty) {
                return true;
            }

            return false;
        }).findFirst();

        return existedDiffSubEntryOptional.isPresent() ? false : true;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
