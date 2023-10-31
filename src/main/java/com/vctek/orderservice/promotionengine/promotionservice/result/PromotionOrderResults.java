package com.vctek.orderservice.promotionengine.promotionservice.result;


import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;

import java.util.List;

public class PromotionOrderResults {
    private AbstractOrderModel order;
    private List<PromotionResultModel> applyAllActionsPromotionResults;
    private double changeFromLastResults;

    public PromotionOrderResults(AbstractOrderModel order,
                                 List<PromotionResultModel> applyAllActionsPromotionResults,
                                 double changeFromLastResults) {

        this.order = order;
        this.applyAllActionsPromotionResults = applyAllActionsPromotionResults;
        this.changeFromLastResults = changeFromLastResults;
    }

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }

    public List<PromotionResultModel> getApplyAllActionsPromotionResults() {
        return applyAllActionsPromotionResults;
    }

    public void setApplyAllActionsPromotionResults(List<PromotionResultModel> applyAllActionsPromotionResults) {
        this.applyAllActionsPromotionResults = applyAllActionsPromotionResults;
    }

    public double getChangeFromLastResults() {
        return changeFromLastResults;
    }

    public void setChangeFromLastResults(double changeFromLastResults) {
        this.changeFromLastResults = changeFromLastResults;
    }
}
