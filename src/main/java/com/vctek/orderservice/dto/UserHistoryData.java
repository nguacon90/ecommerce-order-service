package com.vctek.orderservice.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UserHistoryData extends UserData {
    private String modifiedTime;

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof UserHistoryData)) return false;

        UserHistoryData that = (UserHistoryData) o;

        return new EqualsBuilder()
                .append(getModifiedTime(), that.getModifiedTime())
                .append(getId(), that.getId())
                .append(getName(), that.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getModifiedTime())
                .append(getId())
                .append(getPhone())
                .toHashCode();
    }
}
