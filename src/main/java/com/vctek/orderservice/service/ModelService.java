package com.vctek.orderservice.service;


import com.vctek.orderservice.model.ItemModel;

import java.util.Collection;

public interface ModelService {
    <T> T save(T object);

    void remove(Object model);

    void saveAll(Object... objects);

    void saveAll(Collection objects);

    <T extends ItemModel> T findById(Class clazz, Long id);

    void removeAll(Collection objs);

    void refresh(Object model);

}
