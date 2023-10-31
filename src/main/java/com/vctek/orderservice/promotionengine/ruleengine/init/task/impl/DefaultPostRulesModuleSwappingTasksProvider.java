package com.vctek.orderservice.promotionengine.ruleengine.init.task.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.init.task.PostRulesModuleSwappingTask;
import com.vctek.orderservice.promotionengine.ruleengine.init.task.PostRulesModuleSwappingTasksProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class DefaultPostRulesModuleSwappingTasksProvider implements PostRulesModuleSwappingTasksProvider {
    private List<PostRulesModuleSwappingTask> postRulesModuleSwappingTasks;
    private PostRulesModuleSwappingTask updateRulesStatusPostRulesModuleSwappingTask;

    @Override
    public List<Supplier<Object>> getTasks(RuleEngineActionResult result) {
        return this.getPostRulesModuleSwappingTasks().stream()
                .map(task -> {
                    Supplier<Object> s = () -> task.execute(result);
                    return s;
                })
                .collect(Collectors.toList());
    }

    protected List<PostRulesModuleSwappingTask> getPostRulesModuleSwappingTasks() {
        return this.postRulesModuleSwappingTasks;
    }

    @PostConstruct
    public void setUp() {
        postRulesModuleSwappingTasks = new ArrayList<>();
        postRulesModuleSwappingTasks.add(updateRulesStatusPostRulesModuleSwappingTask);
    }

    @Autowired
    public void setUpdateRulesStatusPostRulesModuleSwappingTask(PostRulesModuleSwappingTask updateRulesStatusPostRulesModuleSwappingTask) {
        this.updateRulesStatusPostRulesModuleSwappingTask = updateRulesStatusPostRulesModuleSwappingTask;
    }
}
