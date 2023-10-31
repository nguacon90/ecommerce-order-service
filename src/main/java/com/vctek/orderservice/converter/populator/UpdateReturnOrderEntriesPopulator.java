package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.kafka.data.ReturnOrdersBillDetailDTO;
import com.vctek.orderservice.dto.UpdateReturnOrderBillDTO;
import com.vctek.orderservice.dto.UpdateReturnOrderBillDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UpdateReturnOrderEntriesPopulator implements Populator<ReturnOrderBillDTO, UpdateReturnOrderBillDTO> {

    @Override
    public void populate(ReturnOrderBillDTO returnOrderBillDTO, UpdateReturnOrderBillDTO target) {
        target.setReturnOrderId(returnOrderBillDTO.getReturnOrderId());
        target.setCompanyId(returnOrderBillDTO.getCompanyId());
        target.setWarehouseId(returnOrderBillDTO.getWarehouseId());

        List<UpdateReturnOrderBillDetail> updateEntries = populateUpdatedEntryLevel(returnOrderBillDTO.getEntries(), false);
        List<UpdateReturnOrderBillDetail> deleteEntries = populateUpdatedEntryLevel(returnOrderBillDTO.getDeleteEntries(), true);
        updateEntries.addAll(deleteEntries);
        target.setEntries(updateEntries);
    }

    private List<UpdateReturnOrderBillDetail> populateUpdatedEntryLevel(List<ReturnOrdersBillDetailDTO> returnOrderBillDetails,
                                                                       final boolean deleted) {
        List<UpdateReturnOrderBillDetail> billDetailData = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(returnOrderBillDetails)) {
            List<ReturnOrdersBillDetailDTO> entryWithoutTopping = returnOrderBillDetails.stream()
                    .filter(bd -> bd.getOrderEntryId() != null && bd.getToppingOptionId() == null)
                    .collect(Collectors.toList());
            List<ReturnOrdersBillDetailDTO> normalEntries = entryWithoutTopping.stream()
                    .filter(bd -> bd.getOrderEntryId() != null && bd.getSubOrderEntryId() == null)
                    .collect(Collectors.toList());
            List<ReturnOrdersBillDetailDTO> comboEntries = entryWithoutTopping.stream()
                    .filter(bd -> bd.getOrderEntryId() != null && bd.getSubOrderEntryId() != null)
                    .collect(Collectors.toList());
            normalEntries.forEach(ne -> {
                if(ne.getQuantity() != null && !ne.getQuantity().equals(ne.getOriginQuantity())) {
                    billDetailData.add(populateBillDetailData(ne, false, deleted));
                }
            });

            Set<Long> entryComboIds = new HashSet<>();
            comboEntries.forEach(ce -> {
                if(!entryComboIds.contains(ce.getOrderEntryId()) && ce.getQuantity() != null &&
                        !ce.getQuantity().equals(ce.getOriginQuantity())) {
                    billDetailData.add(populateBillDetailData(ce, true, deleted));
                    entryComboIds.add(ce.getOrderEntryId());
                }
            });
        }
        return billDetailData;
    }

    private UpdateReturnOrderBillDetail populateBillDetailData(ReturnOrdersBillDetailDTO ce, boolean isComboEntry, boolean deleted) {
        UpdateReturnOrderBillDetail detailData = new UpdateReturnOrderBillDetail();
        detailData.setOrderEntryId(ce.getOrderEntryId());
        Long productId = isComboEntry ? ce.getComboId() : ce.getProductId();
        Integer quantity = isComboEntry ? ce.getComboQty() : ce.getQuantity();
        Integer originQuantity = isComboEntry ? ce.getOriginComboQty() : ce.getOriginQuantity();
        detailData.setProductId(productId);
        detailData.setQuantity(quantity);
        detailData.setOriginQuantity(originQuantity);
        detailData.setDeleted(deleted);
        return detailData;
    }
}
