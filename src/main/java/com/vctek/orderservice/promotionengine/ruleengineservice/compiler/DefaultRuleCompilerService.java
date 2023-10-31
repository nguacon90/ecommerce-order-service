package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.RuleTargetCodeGenerator;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.RuleSourceCodeTranslator;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DefaultRuleCompilerService implements RuleCompilerService {
    private RuleIrVariablesGeneratorFactory ruleIrVariablesGeneratorFactory;
    private RuleTargetCodeGenerator targetCodeGenerator;
    private RuleSourceCodeTranslator ruleSourceCodeTranslator;
    private RuleIrProcessor promotionSourceRuleIrProcessor;
    private RuleCompilerContextFactory<DefaultRuleCompilerContext> ruleCompilerContextFactory;
    private RuleCompilationContext ruleCompilationContext;

    public DefaultRuleCompilerService(RuleIrVariablesGeneratorFactory ruleIrVariablesGeneratorFactory,
                                      @Qualifier("droolsRuleTargetCodeGenerator") RuleTargetCodeGenerator targetCodeGenerator,
                                      RuleSourceCodeTranslator ruleSourceCodeTranslator,
                                      RuleIrProcessor promotionSourceRuleIrProcessor) {
        this.ruleIrVariablesGeneratorFactory = ruleIrVariablesGeneratorFactory;
        this.targetCodeGenerator = targetCodeGenerator;
        this.ruleSourceCodeTranslator = ruleSourceCodeTranslator;
        this.promotionSourceRuleIrProcessor = promotionSourceRuleIrProcessor;
    }

    @Override
    public DroolsRuleModel compile(PromotionSourceRuleModel rule, String moduleName) {
        RuleIrVariablesGenerator variablesGenerator = this.ruleIrVariablesGeneratorFactory.createVariablesGenerator();
        DefaultRuleCompilerContext context = this.ruleCompilerContextFactory.createContext(ruleCompilationContext, rule,
                moduleName, variablesGenerator);
        try {
            RuleIr ruleIr = ruleSourceCodeTranslator.translate(context);
            promotionSourceRuleIrProcessor.process(context, ruleIr);
            return targetCodeGenerator.generate(context, ruleIr);
        } catch (RuleCompilerException | IllegalArgumentException e) {
            String errorMessage = String.format("Exception caught - %s: %s", e.getClass().getName(), e.getMessage());
            throw new RuleCompilerException(errorMessage, e);
        }
    }

    @Autowired
    public void setRuleCompilerContextFactory(RuleCompilerContextFactory<DefaultRuleCompilerContext> ruleCompilerContextFactory) {
        this.ruleCompilerContextFactory = ruleCompilerContextFactory;
    }

    @Autowired
    public void setRuleCompilationContext(RuleCompilationContext ruleCompilationContext) {
        this.ruleCompilationContext = ruleCompilationContext;
    }
}
