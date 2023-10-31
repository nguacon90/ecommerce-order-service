package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.repository.ToppingOptionRepository;
import com.vctek.orderservice.service.ToppingOptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ToppingOptionServiceImpl implements ToppingOptionService {
    private ToppingOptionRepository repository;

    public ToppingOptionServiceImpl(ToppingOptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public ToppingOptionModel save(ToppingOptionModel model) {
        return repository.save(model);
    }

    @Override
    public ToppingOptionModel findById(Long id) {
        Optional<ToppingOptionModel> optionalModel = repository.findById(id);
        return optionalModel.isPresent() ? optionalModel.get() : null;
    }

    @Override
    public ToppingOptionModel findByIdAndOrderEntry(Long id, AbstractOrderEntryModel orderEntryModel) {
        return repository.findByIdAndOrderEntry(id, orderEntryModel);
    }

    @Override
    @Transactional
    public void delete(ToppingOptionModel toppingOptionModel) {
        repository.delete(toppingOptionModel);
    }

}
