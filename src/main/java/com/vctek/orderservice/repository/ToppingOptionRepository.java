package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToppingOptionRepository extends JpaRepository<ToppingOptionModel, Long> {
    ToppingOptionModel findByIdAndOrderEntry(Long id, AbstractOrderEntryModel abstractOrderEntryModel);

    List<ToppingOptionModel> findAllByOrderEntry(AbstractOrderEntryModel entryModel);
}
