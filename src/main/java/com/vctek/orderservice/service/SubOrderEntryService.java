package com.vctek.orderservice.service;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;

import java.util.List;

public interface SubOrderEntryService {
    List<SubOrderEntryModel> saveAll(List<SubOrderEntryModel> subOrderEntryModels);

    SubOrderEntryModel findByOrderEntryAndId(AbstractOrderEntryModel entryModel, Long id);

    List<SubOrderEntryModel> findAllBy(AbstractOrderEntryModel entryModel);
}
