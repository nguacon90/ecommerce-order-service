package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.repository.ModelRepository;
import com.vctek.orderservice.service.ModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collection;

@Service
public class ModelServiceImpl implements ModelService {
    @PersistenceContext
    protected EntityManager entityManager;

    protected ModelRepository modelRepository;

    public ModelServiceImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public <T> T save(T object) {
        return (T) modelRepository.save(object);
    }

    @Override
    @Transactional
    public void remove(Object model) {
        modelRepository.delete(model);
    }

    @Override
    public void saveAll(Object... objects) {
        modelRepository.saveAll(Arrays.asList(objects));
    }

    @Override
    public void saveAll(Collection objects) {
        modelRepository.saveAll(objects);
    }

    @Override
    public <T extends ItemModel> T findById(Class clazz, Long id) {
        return (T) entityManager.find(clazz, id);
    }

    @Override
    @Transactional
    public void removeAll(Collection objs) {
        modelRepository.deleteAll(objs);
    }

    @Override
    @Transactional
    public void refresh(Object model) {
        entityManager.refresh(model);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
