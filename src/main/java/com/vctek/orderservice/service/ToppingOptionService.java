package com.vctek.orderservice.service;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.ToppingOptionModel;

public interface ToppingOptionService {
    ToppingOptionModel save(ToppingOptionModel model);

    ToppingOptionModel findById(Long id);

    ToppingOptionModel findByIdAndOrderEntry(Long id, AbstractOrderEntryModel orderEntryModel);

    void delete(ToppingOptionModel toppingOptionModel);
}
