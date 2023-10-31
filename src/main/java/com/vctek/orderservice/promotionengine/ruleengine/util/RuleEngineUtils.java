package com.vctek.orderservice.promotionengine.ruleengine.util;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

import java.io.File;
import java.util.Objects;

public class RuleEngineUtils {
    public static final String DROOLS_BASE_PATH = "src" +
            File.separatorChar + "main" + File.separatorChar + "resources" + File.separatorChar;
    public static String getRulePath(DroolsRuleModel rule) {
        String rulePackagePath = "";
        if (rule.getRulePackage() != null) {
            rulePackagePath = rule.getRulePackage().replace('.', File.separatorChar);
        }

        return getNormalizedRulePath(DROOLS_BASE_PATH + rulePackagePath + rule.getCode() + "RuleMedia" + ".drl");
    }
    public static String getNormalizedRulePath(String rulePath) {
        return Objects.isNull(rulePath) ? null : rulePath.replace(File.separatorChar, '/');
    }

    public static String stripDroolsMainResources(String normalizedPath) {
        String normalizedDroolsBasePath = getNormalizedRulePath(DROOLS_BASE_PATH);
        if(normalizedDroolsBasePath == null) {
            return normalizedPath;
        }

        return normalizedPath.startsWith(normalizedDroolsBasePath) ?
                normalizedPath.substring(normalizedDroolsBasePath.length()) : normalizedPath;
    }
}
