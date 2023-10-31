package com.vctek.orderservice.elasticsearch.query;

public class NestedSearchQuery {
    private String idQuery;
    private String[] otherFields;
    private String nestedPath;

    public String getIdQuery() {
        return idQuery;
    }

    public void setIdQuery(String idQuery) {
        this.idQuery = idQuery;
    }

    public String[] getOtherFields() {
        return otherFields;
    }

    public void setOtherFields(String[] otherFields) {
        this.otherFields = otherFields;
    }

    public String getNestedPath() {
        return nestedPath;
    }

    public void setNestedPath(String nestedPath) {
        this.nestedPath = nestedPath;
    }
}
