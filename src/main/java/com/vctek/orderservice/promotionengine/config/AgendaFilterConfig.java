package com.vctek.orderservice.promotionengine.config;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.AgendaFilterCreationStrategy;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.agendafilter.impl.DefaultActionTriggeringLimitAgendaFilterCreationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AgendaFilterConfig {

    @Bean(name = "agendaFilterStrategies")
    public List<AgendaFilterCreationStrategy> agendaFilterStrategies(DefaultActionTriggeringLimitAgendaFilterCreationStrategy actionTriggeringLimitAgendaFilterCreationStrategy) {
        List<AgendaFilterCreationStrategy> strategies = new ArrayList<>();
        strategies.add(actionTriggeringLimitAgendaFilterCreationStrategy);
        return strategies;
    }
}
