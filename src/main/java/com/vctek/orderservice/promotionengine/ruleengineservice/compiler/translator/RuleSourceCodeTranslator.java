package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;

public interface RuleSourceCodeTranslator {
    RuleIr translate(RuleCompilerContext ruleCompilerContext);
}
