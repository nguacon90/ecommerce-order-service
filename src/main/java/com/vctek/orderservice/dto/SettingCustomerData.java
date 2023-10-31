package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SettingCustomerData {
    private boolean defaultSetting = false;
    private List<OrderSettingCustomerData> settings = new ArrayList<>();

    public boolean isDefaultSetting() {
        return defaultSetting;
    }

    public void setDefaultSetting(boolean defaultSetting) {
        this.defaultSetting = defaultSetting;
    }

    public List<OrderSettingCustomerData> getSettings() {
        return settings;
    }

    public void setSettings(List<OrderSettingCustomerData> settings) {
        this.settings = settings;
    }
}
