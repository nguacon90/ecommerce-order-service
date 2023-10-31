package com.vctek.orderservice.converter.populator;

import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.kafka.data.ReturnOrdersBillDetailDTO;
import com.vctek.orderservice.dto.UpdateReturnOrderBillDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NormalizeEntriesReturnOrderPopulatorTest {
    private UpdateReturnOrderEntriesPopulator populator;
    private ReturnOrderBillDTO returnBillDto;
    private UpdateReturnOrderBillDTO returnData;
    private Long returnOrderId = 22l;
    private Long companyId = 1l;
    private Long warehouseId = 17l;

    private ReturnOrdersBillDetailDTO generateDetail(Long entryId, Long comboId, Integer qty, Integer originQty,
                                                     Integer comboQty, Integer originComboQty, Long toppingOptionId,
                                                     Long subOrderEntryId, Long productId) {
        ReturnOrdersBillDetailDTO dto = new ReturnOrdersBillDetailDTO();
        dto.setOrderEntryId(entryId);
        dto.setComboId(comboId);
        dto.setOriginComboQty(originComboQty);
        dto.setComboQty(comboQty);
        dto.setQuantity(qty);
        dto.setOriginQuantity(originQty);
        dto.setToppingOptionId(toppingOptionId);
        dto.setSubOrderEntryId(subOrderEntryId);
        dto.setProductId(productId);
        return dto;
    }

    @Before
    public void setUp() {
        returnData = new UpdateReturnOrderBillDTO();
        returnBillDto = new ReturnOrderBillDTO();
        populator = new UpdateReturnOrderEntriesPopulator();
        returnBillDto.setCompanyId(companyId);
        returnBillDto.setReturnOrderId(returnOrderId);
        returnBillDto.setWarehouseId(warehouseId);

    }

    @Test
    public void populateOnlyNormalEntries_OnlyOneEntryChanging() {
        ReturnOrdersBillDetailDTO entry1 = generateDetail(1l, null, 1, 2, null, null,
                null, null, 11l);
        ReturnOrdersBillDetailDTO entry2 = generateDetail(2l, null, 5, 5, null, null,
                null, null, 22l);
        returnBillDto.setEntries(Arrays.asList(entry1, entry2));

        populator.populate(returnBillDto, returnData);

        assertEquals(companyId, returnData.getCompanyId());
        assertEquals(warehouseId, returnData.getWarehouseId());
        assertEquals(returnOrderId, returnData.getReturnOrderId());
        assertEquals(1, returnData.getEntries().size());
        assertEquals(1l, returnData.getEntries().get(0).getOrderEntryId(), 0);
        assertEquals(1, returnData.getEntries().get(0).getQuantity(), 0);
        assertEquals(2, returnData.getEntries().get(0).getOriginQuantity(), 0);
        assertEquals(11l, returnData.getEntries().get(0).getProductId(), 0);
    }

    @Test
    public void populateOnlyComboEntries_NotChangeAnyThing() {
        ReturnOrdersBillDetailDTO entry11 = generateDetail(11l, 1l, 2, 2, 2, 2,
                null, 111l, 11l);
        ReturnOrdersBillDetailDTO entry12 = generateDetail(11l, 1l, 2, 2, 2, 2,
                null, 112l, 12l);

        ReturnOrdersBillDetailDTO entry21 = generateDetail(22l, 2l, 1, 1, 1, 1,
                null, 221l, 21l);
        ReturnOrdersBillDetailDTO entry22 = generateDetail(22l, 2l, 1, 1, 1, 1,
                null, 222l, 22l);
        returnBillDto.setEntries(Arrays.asList(entry11, entry12, entry21, entry22));

        populator.populate(returnBillDto, returnData);

        assertEquals(companyId, returnData.getCompanyId());
        assertEquals(warehouseId, returnData.getWarehouseId());
        assertEquals(returnOrderId, returnData.getReturnOrderId());
        assertEquals(0, returnData.getEntries().size());
//        assertEquals(11l, returnData.getEntries().get(0).getOrderEntryId(), 0);
//        assertEquals(2, returnData.getEntries().get(0).getQuantity(), 0);
//        assertEquals(2, returnData.getEntries().get(0).getOriginQuantity(), 0);
//        assertEquals(1l, returnData.getEntries().get(0).getProductId(), 0);
//
//        assertEquals(22l, returnData.getEntries().get(1).getOrderEntryId(), 0);
//        assertEquals(1, returnData.getEntries().get(1).getQuantity(), 0);
//        assertEquals(1, returnData.getEntries().get(1).getOriginQuantity(), 0);
//        assertEquals(2l, returnData.getEntries().get(1).getProductId(), 0);
    }

    @Test
    public void populateOnlyFnBEntries_NotChangeAnyThing() {
        ReturnOrdersBillDetailDTO entry11 = generateDetail(11l, null, 2, 2, null, null,
                null, null, 11l);
        ReturnOrdersBillDetailDTO entry12 = generateDetail(11l, null, 2, 2, null, null,
                111l, null, 12l);

        ReturnOrdersBillDetailDTO entry13 = generateDetail(11l, 2l, 1, 1, null, null,
                112l, null, 13l);
        returnBillDto.setEntries(Arrays.asList(entry11, entry12, entry13));

        populator.populate(returnBillDto, returnData);

        assertEquals(companyId, returnData.getCompanyId());
        assertEquals(warehouseId, returnData.getWarehouseId());
        assertEquals(returnOrderId, returnData.getReturnOrderId());
        assertEquals(0, returnData.getEntries().size());
//        assertEquals(11l, billData.getEntries().get(0).getOrderEntryId(), 0);
//        assertEquals(2, billData.getEntries().get(0).getQuantity(), 0);
//        assertEquals(2, billData.getEntries().get(0).getOriginQuantity(), 0);
//        assertEquals(11l, billData.getEntries().get(0).getProductId(), 0);
    }
}
