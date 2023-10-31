package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.service.SubOrderEntryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubOrderEntryServiceImpl implements SubOrderEntryService {

    private SubOrderEntryRepository subOrderEntryRepository;

    public SubOrderEntryServiceImpl(SubOrderEntryRepository subOrderEntryRepository) {
        this.subOrderEntryRepository = subOrderEntryRepository;
    }

    @Override
    public List<SubOrderEntryModel> saveAll(List<SubOrderEntryModel> subOrderEntryModels) {
        return subOrderEntryRepository.saveAll(subOrderEntryModels);
    }

    @Override
    public SubOrderEntryModel findByOrderEntryAndId(AbstractOrderEntryModel entryModel, Long id) {
        return subOrderEntryRepository.findByOrderEntryAndId(entryModel, id);
    }

    @Override
    public List<SubOrderEntryModel> findAllBy(AbstractOrderEntryModel entryModel) {
        return subOrderEntryRepository.findAllByOrderEntry(entryModel);
    }
}
