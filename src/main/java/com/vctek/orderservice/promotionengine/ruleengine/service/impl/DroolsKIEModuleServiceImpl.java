package com.vctek.orderservice.promotionengine.ruleengine.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DroolsKIEModuleServiceImpl implements DroolsKIEModuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsKIEModuleServiceImpl.class);
    private DroolsKIEModuleRepository droolsKIEModuleRepository;

    public DroolsKIEModuleServiceImpl(DroolsKIEModuleRepository droolsKIEModuleRepository) {
        this.droolsKIEModuleRepository = droolsKIEModuleRepository;
    }

    @Override
    public DroolsKIEModuleModel findByName(String moduleName) {
        return droolsKIEModuleRepository.findByNameAndActive(moduleName, true);
    }

    @Override
    public DroolsKIEModuleModel findByCompanyId(Long companyId) {
        List<DroolsKIEModuleModel> kieModuleModels = droolsKIEModuleRepository.findAllByCompanyIdOrderByCreationTimeDesc(companyId);
        if(CollectionUtils.isEmpty(kieModuleModels)){
            ErrorCodes err = ErrorCodes.HAS_NOT_PROMOTION_MODULE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(kieModuleModels.size() > 1) {
            LOGGER.warn("Company {} has more than one promotion module", companyId);
        }
        return kieModuleModels.get(0);
    }

    @Override
    public void save(DroolsKIEModuleModel droolsKIEModuleModel) {
        droolsKIEModuleRepository.save(droolsKIEModuleModel);
    }
}
