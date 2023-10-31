package com.vctek.orderservice.dto;

import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;

import java.util.ArrayList;
import java.util.List;

public class LoyaltyRewardSearchExcelData {
    private List<LoyaltyRewardRateSearchModel> dataList = new ArrayList<>();
    private byte[] content;

    public List<LoyaltyRewardRateSearchModel> getDataList() {
        return dataList;
    }

    public void setDataList(List<LoyaltyRewardRateSearchModel> dataList) {
        this.dataList = dataList;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
