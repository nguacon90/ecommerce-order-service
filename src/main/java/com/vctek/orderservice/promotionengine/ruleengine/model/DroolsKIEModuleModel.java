package com.vctek.orderservice.promotionengine.ruleengine.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "drools_kie_module")
@EntityListeners({AuditingEntityListener.class})
public class DroolsKIEModuleModel extends AbstractRuleModuleModel {
    @Column(name = "mvn_group_id")
    private String mvnGroupId;

    @Column(name = "mvn_artifact_id")
    private String mvnArtifactId;

    @Column(name = "mvn_version")
    private String mvnVersion;

    @Column(name = "deployed_mvn_version")
    private String deployedMvnVersion;

    @OneToMany(mappedBy = "droolsKIEModule")
    private Set<DroolsKIEBaseModel> kieBases;

    @Column(name = "creation_time")
    @CreatedDate
    private Date creationTime;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "drools_kie_base_id")
    private DroolsKIEBaseModel defaultKIEBase;

    public String getMvnGroupId() {
        return mvnGroupId;
    }

    public void setMvnGroupId(String mvnGroupId) {
        this.mvnGroupId = mvnGroupId;
    }

    public String getMvnArtifactId() {
        return mvnArtifactId;
    }

    public void setMvnArtifactId(String mvnArtifactId) {
        this.mvnArtifactId = mvnArtifactId;
    }

    public String getMvnVersion() {
        return mvnVersion;
    }

    public void setMvnVersion(String mvnVersion) {
        this.mvnVersion = mvnVersion;
    }

    public Set<DroolsKIEBaseModel> getKieBases() {
        return kieBases;
    }

    public void setKieBases(Set<DroolsKIEBaseModel> kieBases) {
        this.kieBases = kieBases;
    }

    public DroolsKIEBaseModel getDefaultKIEBase() {
        return defaultKIEBase;
    }

    public void setDefaultKIEBase(DroolsKIEBaseModel defaultKIEBase) {
        this.defaultKIEBase = defaultKIEBase;
    }

    public String getDeployedMvnVersion() {
        return deployedMvnVersion;
    }

    public void setDeployedMvnVersion(String deployedMvnVersion) {
        this.deployedMvnVersion = deployedMvnVersion;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
