package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Populator;
import com.vctek.kafka.data.AddressDto;
import com.vctek.orderservice.dto.TrackingHistoryOrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.dto.TrackingOrderDetailData;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.orderservice.util.TrackingHistoryOrderType;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Qualifier("trackingUpdateOrderPopulator")
public class TrackingUpdateOrderPopulator implements Populator<List<TrackingOrderData>, List<TrackingHistoryOrderData>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackingUpdateOrderPopulator.class);
    private ObjectMapper objectMapper;

    @Override
    public void populate(List<TrackingOrderData> compare, List<TrackingHistoryOrderData> historyOrderData) {
        TrackingOrderData oldData = compare.get(0);
        TrackingOrderData newData = compare.get(1);
        if (oldData == null || newData == null) return;
        if (!compare(oldData.getDiscount(), newData.getDiscount()) || !compare(oldData.getDiscountType(), newData.getDiscountType())) {
            addToData(historyOrderData, convertString(oldData.getDiscount()) + convertCurrencyType(oldData.getDiscountType()), convertString(newData.getDiscount()) + convertCurrencyType(newData.getDiscountType()), TrackingHistoryOrderType.DISCOUNT);
        }
        if (!compare(oldData.getVat(), newData.getVat()) || !compare(oldData.getVatType(), newData.getVatType())) {
            addToData(historyOrderData, convertString(oldData.getVat()) + convertCurrencyType(oldData.getVatType()), convertString(newData.getVat()) + convertCurrencyType(newData.getVatType()), TrackingHistoryOrderType.VAT);
        }
        if (!compare(oldData.getDeliveryCost(), newData.getDeliveryCost())) {
            addToData(historyOrderData, convertString(oldData.getDeliveryCost()), convertString(newData.getDeliveryCost()), TrackingHistoryOrderType.DELIVERY_COST);
        }
        if (!compare(oldData.getCompanyShippingFee(), newData.getCompanyShippingFee())) {
            addToData(historyOrderData, convertString(oldData.getCompanyShippingFee()), convertString(newData.getCompanyShippingFee()), TrackingHistoryOrderType.COMPANY_SHIPPING_FEE);
        }
        if (!compare(oldData.getCollaboratorShippingFee(), newData.getCollaboratorShippingFee())) {
            addToData(historyOrderData, convertString(oldData.getCollaboratorShippingFee()), convertString(newData.getCollaboratorShippingFee()), TrackingHistoryOrderType.COLLABORATOR_SHIPPING_FEE);
        }
        if (!compare(oldData.getOrderSourceId(), newData.getOrderSourceId())) {
            addToData(historyOrderData, convertString(oldData.getOrderSourceId()), convertString(newData.getOrderSourceId()), TrackingHistoryOrderType.ORDER_SOURCE);
        }
        if (!compare(oldData.getCustomerNote(), newData.getCustomerNote())) {
            addToData(historyOrderData, oldData.getCustomerNote(), newData.getCustomerNote(), TrackingHistoryOrderType.CUSTOMER_NOTE);
        }
        if (!compare(oldData.getCustomerSupportNote(), newData.getCustomerSupportNote())) {
            addToData(historyOrderData, oldData.getCustomerSupportNote(), newData.getCustomerSupportNote(), TrackingHistoryOrderType.CUSTOMER_SUPPORT_NOTE);
        }
        if (!compare(oldData.getDeliveryDate(), newData.getDeliveryDate())) {
            String oldDate = convertString(oldData.getDeliveryDate());
            String newDate = convertString(newData.getDeliveryDate());
            addToData(historyOrderData, oldDate, newDate, TrackingHistoryOrderType.DELIVERY_DATE);
        }
        if (!compare(oldData.getShippingCompanyId(), newData.getShippingCompanyId())) {
            addToData(historyOrderData, convertString(oldData.getShippingCompanyId()), convertString(newData.getShippingCompanyId()), TrackingHistoryOrderType.SHIPPING_COMPANY_ID);
        }

        compareCustomerAndShippingAddress(historyOrderData, oldData, newData);
        compareOrderEntries(historyOrderData, oldData, newData);
    }

    private String convertCurrencyType(String currencyType) {
        if (CurrencyType.PERCENT.toString().equalsIgnoreCase(currencyType)) {
            return StringUtils.SPACE + CurrencyType.PERCENT.code();
        }
        if (CurrencyType.CASH.toString().equalsIgnoreCase(currencyType)) {
            return StringUtils.SPACE + CurrencyType.CASH.code();
        }
        return StringUtils.EMPTY;
    }

    private String convertString(Date value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return DateUtil.getDateStr(value, DateUtil.ISO_DATE_TIME_PATTERN);
    }

    private String convertString(Long value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return value.toString();
    }

    private String convertString(Double value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return CommonUtils.readTextField(value.toString());
    }

    private boolean compare(Date oldValue, Date newValue) {
        if (oldValue != null && newValue != null) {
            return newValue.getTime() == oldValue.getTime();
        }
        if (oldValue == null && newValue == null) {
            return true;
        }
        return false;
    }

    private boolean compare(String oldValue, String newValue) {
        return (newValue == null ? oldValue == null : newValue.equals(oldValue));
    }

    private boolean compare(Double oldValue, Double newValue) {
        return (newValue == null ? oldValue == null : newValue.equals(oldValue));
    }

    private boolean compare(Long oldValue, Long newValue) {
        return (newValue == null ? oldValue == null : newValue.equals(oldValue));
    }

    private boolean compare(Integer oldValue, Integer newValue) {
        return (newValue == null ? oldValue == null : newValue.equals(oldValue));
    }

    private void compareCustomerAndShippingAddress(List<TrackingHistoryOrderData> historyOrderData, TrackingOrderData oldData, TrackingOrderData newData) {
        if (!compare(oldData.getCustomerId(), newData.getCustomerId())) {
            addToData(historyOrderData, convertString(oldData.getCustomerId()), convertString(newData.getCustomerId()), TrackingHistoryOrderType.CUSTOMER_ID);
        }
        AddressDto oldAddress = oldData.getAddressDto();
        AddressDto newAddress = newData.getAddressDto();
        if (!compare(oldAddress.getName(), newAddress.getName()) || !compare(oldAddress.getPhone(), newAddress.getPhone())
                || !compare(oldAddress.getProvinceId(), newAddress.getProvinceId()) || !compare(oldAddress.getDistrictId(), newAddress.getDistrictId())
                || !compare(oldAddress.getWardId(), newAddress.getWardId()) || !compare(oldAddress.getAddressDetail(), newAddress.getAddressDetail())) {

            addToData(historyOrderData, writeObjectToString(oldAddress), writeObjectToString(newAddress), TrackingHistoryOrderType.SHIPPING_ADDRESS);
        }
    }

    private void addToData(List<TrackingHistoryOrderData> historyOrderData, String oldValue, String newValue, TrackingHistoryOrderType type) {
        TrackingHistoryOrderData historyData = new TrackingHistoryOrderData();
        historyData.setFieldName(type.code());
        historyData.setOldValue(oldValue);
        historyData.setNewValue(newValue);
        historyData.setNote(type.description());
        historyOrderData.add(historyData);
    }

    private String writeObjectToString(Object obj) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.debug("CANNOT WRITE STRING");
            return StringUtils.EMPTY;
        }
    }

    private void compareOrderEntries(List<TrackingHistoryOrderData> historyOrderData, TrackingOrderData oldData, TrackingOrderData newData) {
        List<TrackingOrderDetailData> oldDetails = oldData.getDetails();
        for (TrackingOrderDetailData detail : newData.getDetails()) {
            TrackingOrderDetailData oldDetail = findOldDetailBy(oldDetails, detail);
            if (oldDetail == null) {
                addToData(historyOrderData, null, writeObjectToString(detail), TrackingHistoryOrderType.ADD_PRODUCT);
                continue;
            }
            if (!compare(oldDetail.getQuantity(), detail.getQuantity())) {
                addToData(historyOrderData, writeObjectToString(oldDetail), writeObjectToString(detail), TrackingHistoryOrderType.CHANGE_QUANTITY_PRODUCT);
            }
            if (!compare(oldDetail.getPrice(), detail.getPrice())) {
                addToData(historyOrderData, writeObjectToString(oldDetail), writeObjectToString(detail), TrackingHistoryOrderType.CHANGE_PRICE_PRODUCT);
            }
            if (!compare(oldDetail.getDiscount(), detail.getDiscount()) || !compare(oldDetail.getDiscountType(), detail.getDiscountType())) {
                addToData(historyOrderData, writeObjectToString(oldDetail), writeObjectToString(detail), TrackingHistoryOrderType.CHANGE_DISCOUNT_PRODUCT);
            }
        }
        compareDeletedOldEntries(historyOrderData, oldDetails);
    }

    private TrackingOrderDetailData findOldDetailBy(List<TrackingOrderDetailData> oldDetails, TrackingOrderDetailData detail) {
        Optional<TrackingOrderDetailData> optional = oldDetails.stream().filter(i -> {
            if (i.getEntryId() != null && i.getEntryId().equals(detail.getEntryId())) {
                return true;
            }
            if (i.getSubOrderEntryId() != null && i.getSubOrderEntryId().equals(detail.getSubOrderEntryId())) {
                return true;
            }
            if (i.getToppingOptionId() != null && i.getToppingOptionId().equals(detail.getToppingOptionId())) {
                return true;
            }
            return false;
        }).findFirst();
        if (optional.isPresent()) {
            TrackingOrderDetailData oldDetail = optional.get();
            oldDetails.remove(oldDetail);
            return oldDetail;
        }
        return null;
    }

    private void compareDeletedOldEntries(List<TrackingHistoryOrderData> historyOrderData, List<TrackingOrderDetailData> oldDetails) {
        if (CollectionUtils.isEmpty(oldDetails)) return;
        for (TrackingOrderDetailData oldDetail : oldDetails) {
            addToData(historyOrderData, writeObjectToString(oldDetail), null, TrackingHistoryOrderType.DELETED_PRODUCT);
        }
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
