package com.vctek.orderservice.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmployeeChangeData {
    private Map<String, List<OrderHistoryData>> orderHistory;
    private Map<String, Set<UserHistoryData>> listUserData;

    public Map<String, List<OrderHistoryData>> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(Map<String, List<OrderHistoryData>> orderHistory) {
        this.orderHistory = orderHistory;
    }

    public Map<String, Set<UserHistoryData>> getListUserData() {
        return listUserData;
    }

    public void setListUserData(Map<String, Set<UserHistoryData>> listUserData) {
        this.listUserData = listUserData;
    }
}
