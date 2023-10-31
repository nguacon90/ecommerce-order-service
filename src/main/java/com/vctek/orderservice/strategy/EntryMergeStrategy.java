package com.vctek.orderservice.strategy;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;

import java.util.List;

public interface EntryMergeStrategy {
    AbstractOrderEntryModel getEntryToMerge(List<AbstractOrderEntryModel> toEntries, AbstractOrderEntryModel entry);

}
