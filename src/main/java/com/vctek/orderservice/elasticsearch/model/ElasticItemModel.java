package com.vctek.orderservice.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Id;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticItemModel implements Serializable {
    @Id
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
