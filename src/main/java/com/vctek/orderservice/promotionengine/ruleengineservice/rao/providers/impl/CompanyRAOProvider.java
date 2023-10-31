package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CompanyRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component("companyRAOProvider")
public class CompanyRAOProvider implements RAOProvider<AbstractOrderModel> {

    @Override
    public Set expandFactModel(AbstractOrderModel orderModel) {
        CompanyRAO companyRAO = new CompanyRAO();
        companyRAO.setId(orderModel.getCompanyId());
        return Collections.singleton(companyRAO);
    }
}
