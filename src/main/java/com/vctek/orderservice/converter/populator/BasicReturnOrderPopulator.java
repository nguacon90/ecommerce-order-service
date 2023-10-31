package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.ReturnOrderData;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("basicReturnOrderPopulator")
public class BasicReturnOrderPopulator implements Populator<ReturnOrderModel, ReturnOrderData> {
    private LoyaltyService loyaltyService;

    @Override
    public void populate(ReturnOrderModel returnOrderModel, ReturnOrderData returnOrderData) {
        returnOrderData.setId(returnOrderModel.getId());
        returnOrderData.setBillId(returnOrderModel.getBillId());
        returnOrderData.setNote(returnOrderModel.getNote());
        OrderModel originOrder = returnOrderModel.getOriginOrder();
        returnOrderData.setOriginOrderCode(originOrder.getCode());
        returnOrderData.setOriginOrderType(originOrder.getType());
        returnOrderData.setCustomerId(originOrder.getCustomerId());
        returnOrderData.setCreatedBy(returnOrderModel.getCreatedBy());
        returnOrderData.setCreatedTime(returnOrderModel.getCreatedTime());
        returnOrderData.setConversionRate(returnOrderModel.getConversionRate());
        returnOrderData.setCompensateRevert(returnOrderModel.getCompensateRevert());
        returnOrderData.setVat(returnOrderModel.getVat());
        returnOrderData.setShippingFee(CommonUtils.readValue(returnOrderModel.getShippingFee()));
        returnOrderData.setCompanyShippingFee(CommonUtils.readValue(returnOrderModel.getCompanyShippingFee()));
        returnOrderData.setCollaboratorShippingFee(CommonUtils.readValue(returnOrderModel.getCollaboratorShippingFee()));
        returnOrderData.setCardNumber(originOrder.getCardNumber());

        if (StringUtils.isNotBlank(originOrder.getCardNumber())) {
            populateRefundRevert(returnOrderModel, returnOrderData);
        }
    }


    private void populateRefundRevert(ReturnOrderModel returnOrderModel, ReturnOrderData returnOrderData) {
        OrderModel originOrder = returnOrderModel.getOriginOrder();
        LoyaltyCardData loyaltyCardData = loyaltyService.findByCardNumber(originOrder.getCardNumber(), originOrder.getCompanyId());
        returnOrderData.setAvailablePoint(loyaltyCardData.getPointAmount());
        returnOrderData.setPendingPoint(loyaltyCardData.getPendingAmount());
        if (returnOrderModel.getRefundAmount() != null) {
            double refundPoint =returnOrderModel.getRefundAmount() / returnOrderModel.getConversionRate();
            returnOrderData.setRefundPoint(refundPoint);
        }

        if (returnOrderModel.getRevertAmount() != null && returnOrderModel.getRevertAmount() != 0) {
            double revertPoint = returnOrderModel.getRevertAmount() / returnOrderModel.getConversionRate();
            returnOrderData.setRevertPoint(revertPoint);
        }

        if(returnOrderModel.getRedeemAmount()!=null && returnOrderModel.getRedeemAmount() != 0){
            double redeemPoint = returnOrderModel.getRedeemAmount() / returnOrderModel.getConversionRate();
            returnOrderData.setRedeemPoint(redeemPoint);
        }
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }
}
