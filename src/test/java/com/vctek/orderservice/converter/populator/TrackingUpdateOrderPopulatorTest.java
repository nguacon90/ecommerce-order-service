package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.kafka.data.AddressDto;
import com.vctek.orderservice.dto.TrackingHistoryOrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.dto.TrackingOrderDetailData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TrackingUpdateOrderPopulatorTest {
    private TrackingUpdateOrderPopulator populator;
    private List<TrackingOrderData> trackingOrderDataList;
    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new TrackingUpdateOrderPopulator();
        populator.setObjectMapper(objectMapper);
        trackingOrderDataList = new ArrayList<>();
        TrackingOrderData oldData = new TrackingOrderData();
        TrackingOrderData newData = new TrackingOrderData();
        List<TrackingOrderDetailData> oldDetails = new ArrayList<>();
        TrackingOrderDetailData oldDetailData = new TrackingOrderDetailData();
        oldDetailData.setDiscount(2000d);
        oldDetailData.setPrice(2000d);
        oldDetailData.setQuantity(2);
        oldDetailData.setEntryId(3L);
        oldDetailData.setProductId(2L);
        TrackingOrderDetailData oldDetailData1 = new TrackingOrderDetailData();
        oldDetailData1.setDiscount(2000d);
        oldDetailData1.setPrice(2000d);
        oldDetailData1.setQuantity(2);
        oldDetailData1.setEntryId(1L);
        oldDetailData1.setProductId(2L);
        oldDetails.add(oldDetailData1);
        oldDetails.add(oldDetailData);
        oldData.setDetails(oldDetails);
        AddressDto oldDddressDto = new AddressDto();
        oldDddressDto.setName("name1");
        oldDddressDto.setPhone("phone1");
        oldDddressDto.setProvinceId(2L);
        oldDddressDto.setDistrictId(2L);
        oldDddressDto.setWardId(2L);
        oldDddressDto.setAddressDetail("detail");
        oldData.setAddressDto(oldDddressDto);
        newData.setDiscount(2000d);
        newData.setVat(2000d);
        newData.setDeliveryCost(2000d);
        newData.setCompanyShippingFee(2000d);
        newData.setCollaboratorShippingFee(2000d);
        newData.setOrderSourceId(2L);
        newData.setCustomerNote("note");
        newData.setCustomerSupportNote("note");
        newData.setDeliveryDate(Calendar.getInstance().getTime());
        newData.setShippingCompanyId(2L);
        newData.setCustomerId(2L);
        AddressDto addressDto = new AddressDto();
        addressDto.setName("name");
        addressDto.setPhone("phone");
        addressDto.setProvinceId(2L);
        addressDto.setDistrictId(2L);
        addressDto.setWardId(2L);
        addressDto.setAddressDetail("detail");
        newData.setAddressDto(addressDto);
        List<TrackingOrderDetailData> details = new ArrayList<>();
        TrackingOrderDetailData detailData = new TrackingOrderDetailData();
        detailData.setDiscount(2000d);
        detailData.setPrice(2000d);
        detailData.setQuantity(2);
        detailData.setEntryId(2L);
        detailData.setProductId(2L);
        TrackingOrderDetailData detailData1 = new TrackingOrderDetailData();
        detailData1.setDiscount(200d);
        detailData1.setPrice(20000d);
        detailData1.setQuantity(1);
        detailData1.setEntryId(1L);
        detailData1.setProductId(2L);
        details.add(detailData1);
        details.add(detailData);
        newData.setDetails(details);
        trackingOrderDataList.add(oldData);
        trackingOrderDataList.add(newData);
    }

    @Test
    public void populate() {
        List<TrackingHistoryOrderData> historyOrderData = new ArrayList<>();
        populator.populate(trackingOrderDataList, historyOrderData);
        assertEquals(17, historyOrderData.size(), 0);
    }
}
