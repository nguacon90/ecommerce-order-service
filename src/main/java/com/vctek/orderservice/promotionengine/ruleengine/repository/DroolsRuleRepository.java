package com.vctek.orderservice.promotionengine.ruleengine.repository;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface DroolsRuleRepository extends JpaRepository<DroolsRuleModel, Long> {

    @Query(value = "FROM DroolsRuleModel as dr JOIN dr.kieBase as kb " +
            " JOIN kb.droolsKIEModule as km WHERE dr.code = ?1 AND km.name = ?2 ")
    DroolsRuleModel findByCodeAndModuleName(String code, String moduleName);

    @Query(value = "FROM DroolsRuleModel as dr JOIN dr.kieBase as kb " +
            " JOIN kb.droolsKIEModule as km WHERE dr.code = ?1 AND km.name = ?2 AND dr.active = ?3")
    DroolsRuleModel findByCodeAndModuleNameAndActive(String code, String moduleName, boolean active);

    @Query(value = "FROM DroolsRuleModel WHERE code = ?1 AND active = ?2")
    DroolsRuleModel findByCodeAndActive(String code, boolean active);


    @Query(value = "SELECT * FROM drools_rule as dr JOIN promotion_source_rule as psr ON " +
            " dr.promotion_source_rule_id = psr.id WHERE dr.drools_kie_base_id = ?1 AND dr.active = 1 AND " +
            " (psr.end_date >= ?2 OR psr.end_date IS NULL) AND psr.active = 1 AND dr.current_version = 1", nativeQuery = true)
    List<DroolsRuleModel> findAllByKieBaseAndActiveAndDate(Long kieBaseId, Date currentDate);


    @Query(value = "SELECT * FROM drools_rule as dr JOIN drools_kie_base as kb " +
            " ON dr.drools_kie_base_id = kb.id JOIN drools_kie_module as km " +
            " ON kb.drools_kie_module_id = km.id  WHERE km.name = ?1 dr.version <= ?2 AND dr.active = 1", nativeQuery = true)
    List<DroolsRuleModel> getActiveRulesForVersion(String moduleName, long version);

    @Query(value = "SELECT * FROM drools_rule as dr JOIN drools_kie_base as kb " +
            " ON dr.drools_kie_base_id = kb.id JOIN drools_kie_module as km " +
            " ON kb.drools_kie_module_id = km.id  WHERE km.name = ?1 AND dr.version <= ?2", nativeQuery = true)
    List<DroolsRuleModel> getRulesForVersion(String moduleName, long version);


    List<DroolsRuleModel> findByUuidIn(List<String> uuids);
}
