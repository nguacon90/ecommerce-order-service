package com.vctek.orderservice.promotionengine.ruleengine;

import java.io.Serializable;
import java.util.Collection;

public class RuleEngineActionResult implements Serializable {
    private String moduleName;
    private boolean actionFailed;
    private String deployedVersion;
    private String oldVersion;
    private Collection<ResultItem> results;
    private ExecutionContext executionContext;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isActionFailed() {
        return actionFailed;
    }

    public void setActionFailed(boolean actionFailed) {
        this.actionFailed = actionFailed;
    }

    public String getDeployedVersion() {
        return deployedVersion;
    }

    public void setDeployedVersion(String deployedVersion) {
        this.deployedVersion = deployedVersion;
    }

    public String getOldVersion() {
        return oldVersion;
    }

    public void setOldVersion(String oldVersion) {
        this.oldVersion = oldVersion;
    }

    public Collection<ResultItem> getResults() {
        return results;
    }

    public void setResults(Collection<ResultItem> results) {
        this.results = results;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public String getMessagesAsString(final MessageLevel level) {
        final StringBuilder sb = new StringBuilder("RulesModule:").append(getModuleName());
        if (results != null) {
            int n = 0;
            for (final ResultItem item : results) {
                if (level == null || level.equals(item.getLevel())) {
                    final String messageLine = String.format("%s line %d : %s", item.getPath(), Integer.valueOf(item.getLine()),
                            item.getMessage());
                    sb.append(results.size() > 1 ? String.format("%d) ", Integer.valueOf(++n)) : "").append(messageLine)
                            .append(System.lineSeparator());
                }
            }
        }
        return sb.toString();
    }
}
