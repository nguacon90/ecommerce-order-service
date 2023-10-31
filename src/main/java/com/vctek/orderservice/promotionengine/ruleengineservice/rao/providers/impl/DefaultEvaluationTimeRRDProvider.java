package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.EvaluationTimeRRD;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Component("evaluationTimeRRDProvider")
public class DefaultEvaluationTimeRRDProvider implements RAOProvider {

    public Set expandFactModel(Object modelFact) {
        if (modelFact instanceof Date) {
            EvaluationTimeRRD evaluationTimeRRD = new EvaluationTimeRRD();
            evaluationTimeRRD.setEvaluationTime(((Date)modelFact).getTime());
            return Collections.singleton(evaluationTimeRRD);
        } else {
            return Collections.emptySet();
        }
    }
}
